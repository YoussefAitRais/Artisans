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

@Service
public class QuoteService {

    // ============ DEPENDENCIES ============
    
    private final QuoteRepository quoteRepository;
    private final ServiceRequestRepository serviceRequestRepository;
    private final EngagementRepository engagementRepository;


    public QuoteService(QuoteRepository quoteRepository,
                        ServiceRequestRepository serviceRequestRepository,
                        EngagementRepository engagementRepository) {
        this.quoteRepository = quoteRepository;
        this.serviceRequestRepository = serviceRequestRepository;
        this.engagementRepository = engagementRepository;
    }

    // ============ ARTISAN OPERATIONS ============


    @Transactional
    public QuoteResponse createQuoteAsArtisan(Artisan artisan, Long requestId, QuoteCreateRequest createRequest) {
        ServiceRequest serviceRequest = findServiceRequestById(requestId);
        
        validateQuoteCreation(serviceRequest, artisan, requestId);
        
        Quote quote = buildNewQuote(serviceRequest, artisan, createRequest);
        Quote savedQuote = quoteRepository.save(quote);
        
        updateServiceRequestStatus(serviceRequest);
        
        return mapToResponse(savedQuote);
    }


    @Transactional
    public QuoteResponse updateQuoteAsArtisan(Artisan artisan, Long quoteId, QuoteUpdateRequest updateRequest) {
        Quote quote = findQuoteByIdAndArtisan(quoteId, artisan.getId());
        
        validateQuoteUpdate(quote);
        
        applyQuoteUpdates(quote, updateRequest);
        
        return mapToResponse(quote);
    }


    @Transactional
    public void deleteQuoteAsArtisan(Artisan artisan, Long quoteId) {
        Quote quote = findQuoteByIdAndArtisan(quoteId, artisan.getId());
        
        validateQuoteDeletion(quote);
        
        quoteRepository.delete(quote);
    }


    @Transactional(readOnly = true)
    public Page<QuoteResponse> getArtisanQuotes(Artisan artisan, Pageable pageable) {
        return quoteRepository.findByArtisan_Id(artisan.getId(), pageable)
                .map(this::mapToResponse);
    }

    // ============ CLIENT OPERATIONS ============


    @Transactional(readOnly = true)
    public Page<QuoteResponse> getQuotesForClientRequest(Client client, Long requestId, Pageable pageable) {
        ServiceRequest serviceRequest = findServiceRequestByIdAndClient(requestId, client.getId());
        return quoteRepository.findByRequest_Id(serviceRequest.getId(), pageable)
                .map(this::mapToResponse);
    }


    @Transactional
    public QuoteResponse acceptQuote(Client client, Long quoteId) {
        Quote quote = findQuoteById(quoteId);
        ServiceRequest serviceRequest = quote.getRequest();
        
        validateQuoteAcceptance(client, serviceRequest, quote);
        
        processQuoteAcceptance(quote, serviceRequest);
        createEngagementIfNeeded(quote, serviceRequest);
        
        return mapToResponse(quote);
    }

    // ============ ADMINISTRATIVE OPERATIONS ============

    @Transactional(readOnly = true)
    public Page<QuoteResponse> getQuotesByRequestForAdmin(Long requestId, Pageable pageable) {
        return quoteRepository.findByRequest_Id(requestId, pageable)
                .map(this::mapToResponse);
    }


    @Transactional(readOnly = true)
    public Page<QuoteResponse> getQuotesByArtisanForAdmin(Long artisanId, Pageable pageable) {
        return quoteRepository.findByArtisan_Id(artisanId, pageable)
                .map(this::mapToResponse);
    }


    @Transactional
    public void deleteQuoteAsAdmin(Long quoteId) {
        if (!quoteRepository.existsById(quoteId)) {
            throw new IllegalArgumentException("Quote not found with ID: " + quoteId);
        }
        quoteRepository.deleteById(quoteId);
    }

    // ============ PRIVATE HELPER METHODS ============
    

    private ServiceRequest findServiceRequestById(Long requestId) {
        return serviceRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Service request not found with ID: " + requestId));
    }
    

    private ServiceRequest findServiceRequestByIdAndClient(Long requestId, Long clientId) {
        return serviceRequestRepository.findByIdAndClient_Id(requestId, clientId)
                .orElseThrow(() -> new IllegalArgumentException("Service request not found for this client"));
    }
    

    private Quote findQuoteById(Long quoteId) {
        return quoteRepository.findById(quoteId)
                .orElseThrow(() -> new IllegalArgumentException("Quote not found with ID: " + quoteId));
    }
    

    private Quote findQuoteByIdAndArtisan(Long quoteId, Long artisanId) {
        return quoteRepository.findByIdAndArtisan_Id(quoteId, artisanId)
                .orElseThrow(() -> new IllegalArgumentException("Quote not found for this artisan"));
    }
    

    private void validateQuoteCreation(ServiceRequest serviceRequest, Artisan artisan, Long requestId) {
        if (!isRequestAcceptingQuotes(serviceRequest)) {
            throw new IllegalStateException("Request is not accepting quotes");
        }
        
        if (quoteRepository.existsByRequest_IdAndArtisan_Id(requestId, artisan.getId())) {
            throw new IllegalStateException("You already sent a quote for this request");
        }
    }
    

    private boolean isRequestAcceptingQuotes(ServiceRequest serviceRequest) {
        RequestStatus status = serviceRequest.getStatus();
        return status == RequestStatus.PENDING || status == RequestStatus.RESPONDED;
    }
    

    private Quote buildNewQuote(ServiceRequest serviceRequest, Artisan artisan, QuoteCreateRequest createRequest) {
        Quote quote = new Quote();
        quote.setRequest(serviceRequest);
        quote.setArtisan(artisan);
        quote.setPrice(createRequest.getPrice());
        quote.setEstimatedDays(createRequest.getEstimatedDays());
        quote.setMessage(createRequest.getMessage());
        quote.setStatus(QuoteStatus.SENT);
        return quote;
    }
    

    private void updateServiceRequestStatus(ServiceRequest serviceRequest) {
        if (serviceRequest.getStatus() == RequestStatus.PENDING) {
            serviceRequest.setStatus(RequestStatus.RESPONDED);
        }
    }
    

    private void validateQuoteUpdate(Quote quote) {
        if (quote.getStatus() != QuoteStatus.SENT) {
            throw new IllegalStateException("Only SENT quotes can be updated");
        }
    }
    

    private void applyQuoteUpdates(Quote quote, QuoteUpdateRequest updateRequest) {
        if (updateRequest.getPrice() != null) {
            quote.setPrice(updateRequest.getPrice());
        }
        if (updateRequest.getEstimatedDays() != null) {
            quote.setEstimatedDays(updateRequest.getEstimatedDays());
        }
        if (updateRequest.getMessage() != null) {
            quote.setMessage(updateRequest.getMessage());
        }
    }
    

    private void validateQuoteDeletion(Quote quote) {
        if (quote.getStatus() != QuoteStatus.SENT) {
            throw new IllegalStateException("Only SENT quotes can be deleted");
        }
    }
    

    private void validateQuoteAcceptance(Client client, ServiceRequest serviceRequest, Quote quote) {
        if (!serviceRequest.getClient().getId().equals(client.getId())) {
            throw new IllegalStateException("You can only accept quotes for your own request");
        }
        
        if (isRequestClosed(serviceRequest)) {
            throw new IllegalStateException("Request can no longer accept quotes");
        }
        
        if (quote.getStatus() != QuoteStatus.SENT) {
            throw new IllegalStateException("Only a SENT quote can be accepted");
        }
    }
    

    private boolean isRequestClosed(ServiceRequest serviceRequest) {
        RequestStatus status = serviceRequest.getStatus();
        return status == RequestStatus.ACCEPTED || 
               status == RequestStatus.CANCELLED || 
               status == RequestStatus.COMPLETED;
    }
    

    private void processQuoteAcceptance(Quote acceptedQuote, ServiceRequest serviceRequest) {
        List<Quote> allQuotes = quoteRepository.findByRequest_Id(serviceRequest.getId());
        
        for (Quote quote : allQuotes) {
            if (quote.getId().equals(acceptedQuote.getId())) {
                quote.setStatus(QuoteStatus.ACCEPTED);
            } else if (quote.getStatus() == QuoteStatus.SENT) {
                quote.setStatus(QuoteStatus.REJECTED);
            }
        }
        
        serviceRequest.setStatus(RequestStatus.ACCEPTED);
    }
    

    private void createEngagementIfNeeded(Quote quote, ServiceRequest serviceRequest) {
        if (!engagementRepository.existsByRequest_Id(serviceRequest.getId())) {
            Engagement engagement = buildEngagement(quote, serviceRequest);
            engagementRepository.save(engagement);
        }
    }
    

    private Engagement buildEngagement(Quote quote, ServiceRequest serviceRequest) {
        Engagement engagement = new Engagement();
        engagement.setRequest(serviceRequest);
        engagement.setQuote(quote);
        engagement.setClient(serviceRequest.getClient());
        engagement.setArtisan(quote.getArtisan());
        engagement.setAgreedPrice(quote.getPrice());
        engagement.setStatus(EngagementStatus.PENDING_CONFIRMATION);
        return engagement;
    }


    private QuoteResponse mapToResponse(Quote quote) {
        return new QuoteResponse(
                quote.getId(),
                quote.getRequest().getId(),
                quote.getArtisan().getId(),
                quote.getPrice(),
                quote.getEstimatedDays(),
                quote.getMessage(),
                quote.getStatus(),
                quote.getCreatedAt()
        );
    }
}
