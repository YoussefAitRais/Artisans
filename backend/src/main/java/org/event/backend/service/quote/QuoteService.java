package org.event.backend.service.quote;

import org.event.backend.dto.quote.QuoteCreateRequest;
import org.event.backend.dto.quote.QuoteResponse;
import org.event.backend.dto.quote.QuoteUpdateRequest;
import org.event.backend.entity.Artisan;
import org.event.backend.entity.Client;
import org.event.backend.entity.Engagement;
import org.event.backend.entity.EngagementStatus;
import org.event.backend.entity.Quote;
import org.event.backend.entity.QuoteStatus;
import org.event.backend.entity.RequestStatus;
import org.event.backend.entity.ServiceRequest;
import org.event.backend.repository.EngagementRepository;
import org.event.backend.repository.QuoteRepository;
import org.event.backend.repository.ServiceRequestRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Business rules for quotes (artisan proposals for a service request).
 */
@Service
public class QuoteService {

    private final QuoteRepository quoteRepository;
    private final ServiceRequestRepository requestRepository;
    private final EngagementRepository engagementRepository;

    public QuoteService(QuoteRepository quoteRepository,
                        ServiceRequestRepository requestRepository,
                        EngagementRepository engagementRepository) {
        this.quoteRepository = quoteRepository;
        this.requestRepository = requestRepository;
        this.engagementRepository = engagementRepository;
    }

    // ---------- Artisan actions ----------

    @Transactional
    public QuoteResponse createAsArtisan(Artisan artisan, Long requestId, QuoteCreateRequest req) {
        ServiceRequest sr = requestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));

        // Request must be open to receive quotes
        if (sr.getStatus() != RequestStatus.PENDING && sr.getStatus() != RequestStatus.RESPONDED) {
            throw new IllegalStateException("Request is not accepting quotes");
        }

        // Prevent duplicate quotes by same artisan for same request
        if (quoteRepository.existsByRequest_IdAndArtisan_Id(requestId, artisan.getId())) {
            throw new IllegalStateException("You already sent a quote for this request");
        }

        Quote q = new Quote();
        q.setRequest(sr);
        q.setArtisan(artisan);
        q.setPrice(req.getPrice());
        q.setEstimatedDays(req.getEstimatedDays());
        q.setMessage(req.getMessage());
        q.setStatus(QuoteStatus.SENT);

        Quote saved = quoteRepository.save(q);

        // First quote? mark request as RESPONDED
        if (sr.getStatus() == RequestStatus.PENDING) {
            sr.setStatus(RequestStatus.RESPONDED);
        }

        return toResponse(saved);
    }

    @Transactional
    public QuoteResponse updateAsArtisan(Artisan artisan, Long quoteId, QuoteUpdateRequest req) {
        Quote q = quoteRepository.findByIdAndArtisan_Id(quoteId, artisan.getId())
                .orElseThrow(() -> new IllegalArgumentException("Quote not found"));

        if (q.getStatus() != QuoteStatus.SENT) {
            throw new IllegalStateException("Only SENT quotes can be updated");
        }

        if (req.getPrice() != null) q.setPrice(req.getPrice());
        if (req.getEstimatedDays() != null) q.setEstimatedDays(req.getEstimatedDays());
        if (req.getMessage() != null) q.setMessage(req.getMessage());

        return toResponse(q);
    }

    @Transactional
    public void deleteAsArtisan(Artisan artisan, Long quoteId) {
        Quote q = quoteRepository.findByIdAndArtisan_Id(quoteId, artisan.getId())
                .orElseThrow(() -> new IllegalArgumentException("Quote not found"));

        if (q.getStatus() != QuoteStatus.SENT) {
            throw new IllegalStateException("Only SENT quotes can be deleted");
        }
        quoteRepository.delete(q);
    }

    @Transactional(readOnly = true)
    public Page<QuoteResponse> myQuotesAsArtisan(Artisan artisan, Pageable pageable) {
        return quoteRepository.findByArtisan_Id(artisan.getId(), pageable).map(this::toResponse);
    }

    // ---------- Client actions ----------

    @Transactional(readOnly = true)
    public Page<QuoteResponse> listQuotesForMyRequest(Client client, Long requestId, Pageable pageable) {
        ServiceRequest sr = requestRepository.findByIdAndClient_Id(requestId, client.getId())
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));
        return quoteRepository.findByRequest_Id(sr.getId(), pageable).map(this::toResponse);
    }

    @Transactional
    public QuoteResponse acceptQuote(Client client, Long quoteId) {
        Quote q = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new IllegalArgumentException("Quote not found"));

        ServiceRequest sr = q.getRequest();

        // Owner check
        if (!sr.getClient().getId().equals(client.getId())) {
            throw new IllegalStateException("You can only accept quotes for your own request");
        }

        // Request must still be open (not accepted/cancelled/completed)
        if (sr.getStatus() == RequestStatus.ACCEPTED ||
                sr.getStatus() == RequestStatus.CANCELLED ||
                sr.getStatus() == RequestStatus.COMPLETED) {
            throw new IllegalStateException("Request can no longer accept quotes");
        }

        if (q.getStatus() != QuoteStatus.SENT) {
            throw new IllegalStateException("Only a SENT quote can be accepted");
        }

        // Accept current quote; reject others
        List<Quote> all = quoteRepository.findByRequest_Id(sr.getId());
        for (Quote other : all) {
            if (other.getId().equals(q.getId())) {
                other.setStatus(QuoteStatus.ACCEPTED);
            } else {
                if (other.getStatus() == QuoteStatus.SENT) {
                    other.setStatus(QuoteStatus.REJECTED);
                }
            }
        }

        // Update request state
        sr.setStatus(RequestStatus.ACCEPTED);

        // Create engagement if not exists for this request
        if (!engagementRepository.existsByRequest_Id(sr.getId())) {
            Engagement e = new Engagement();
            e.setRequest(sr);
            e.setQuote(q);
            e.setClient(sr.getClient());
            e.setArtisan(q.getArtisan());
            e.setAgreedPrice(q.getPrice());
            e.setStatus(EngagementStatus.PENDING_CONFIRMATION);
            engagementRepository.save(e);
        }

        return toResponse(q);
    }

    // ---------- Admin ----------

    @Transactional(readOnly = true)
    public Page<QuoteResponse> adminListByRequest(Long requestId, Pageable pageable) {
        return quoteRepository.findByRequest_Id(requestId, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<QuoteResponse> adminListByArtisan(Long artisanId, Pageable pageable) {
        return quoteRepository.findByArtisan_Id(artisanId, pageable).map(this::toResponse);
    }

    @Transactional
    public void adminDelete(Long id) {
        if (!quoteRepository.existsById(id)) {
            throw new IllegalArgumentException("Quote not found");
        }
        quoteRepository.deleteById(id);
    }

    // ---------- Mapper ----------

    private QuoteResponse toResponse(Quote q) {
        return new QuoteResponse(
                q.getId(),
                q.getRequest().getId(),
                q.getArtisan().getId(),
                q.getPrice(),
                q.getEstimatedDays(),
                q.getMessage(),
                q.getStatus(),
                q.getCreatedAt()
        );
    }
}
