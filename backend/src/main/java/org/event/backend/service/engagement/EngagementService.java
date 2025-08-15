package org.event.backend.service.engagement;

import org.event.backend.dto.engagement.EngagementConfirmRequest;
import org.event.backend.dto.engagement.EngagementResponse;
import org.event.backend.entity.*;
import org.event.backend.repository.EngagementRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Business rules for engagements (bookings). */
@Service
public class EngagementService {

    private final EngagementRepository engagementRepository;

    public EngagementService(EngagementRepository engagementRepository) {
        this.engagementRepository = engagementRepository;
    }

    // ---- Listing for dashboards ----
    @Transactional(readOnly = true)
    public Page<EngagementResponse> myEngagements(Utilisateur current, Pageable pageable) {
        if (current instanceof Client c) {
            return engagementRepository.findByClient_Id(c.getId(), pageable).map(this::toResponse);
        } else if (current instanceof Artisan a) {
            return engagementRepository.findByArtisan_Id(a.getId(), pageable).map(this::toResponse);
        }
        throw new IllegalStateException("Only CLIENT or ARTISAN can list engagements");
    }

    @Transactional(readOnly = true)
    public EngagementResponse getOne(Utilisateur current, Long id) {
        Engagement e = fetchOwned(current, id);
        return toResponse(e);
    }

    // ---- Transitions ----

    /** Artisan confirms schedule: PENDING_CONFIRMATION -> SCHEDULED */
    @Transactional
    public EngagementResponse confirmAsArtisan(Artisan artisan, Long id, EngagementConfirmRequest req) {
        Engagement e = engagementRepository.findByIdAndArtisan_Id(id, artisan.getId())
                .orElseThrow(() -> new IllegalArgumentException("Engagement not found"));
        if (e.getStatus() != EngagementStatus.PENDING_CONFIRMATION) {
            throw new IllegalStateException("Only PENDING_CONFIRMATION can be confirmed");
        }
        if (req.getStartDate() == null || req.getEndDate() == null ||
                !req.getStartDate().isBefore(req.getEndDate())) {
            throw new IllegalArgumentException("Invalid dates: start must be before end");
        }
        e.setStartDate(req.getStartDate());
        e.setEndDate(req.getEndDate());
        e.setStatus(EngagementStatus.SCHEDULED);
        return toResponse(e); // managed entity
    }

    /** Start work: SCHEDULED -> IN_PROGRESS (either party may trigger) */
    @Transactional
    public EngagementResponse start(Utilisateur current, Long id) {
        Engagement e = fetchOwned(current, id);
        if (e.getStatus() != EngagementStatus.SCHEDULED) {
            throw new IllegalStateException("Only SCHEDULED can be started");
        }
        e.setStatus(EngagementStatus.IN_PROGRESS);
        return toResponse(e);
    }

    /** Complete work: IN_PROGRESS -> COMPLETED (prefer client) */
    @Transactional
    public EngagementResponse completeAsClient(Client client, Long id) {
        Engagement e = engagementRepository.findByIdAndClient_Id(id, client.getId())
                .orElseThrow(() -> new IllegalArgumentException("Engagement not found"));
        if (e.getStatus() != EngagementStatus.IN_PROGRESS) {
            throw new IllegalStateException("Only IN_PROGRESS can be completed");
        }
        e.setStatus(EngagementStatus.COMPLETED);
        return toResponse(e);
    }

    /** Cancel before start: PENDING_CONFIRMATION/SCHEDULED -> CANCELLED (either) */
    @Transactional
    public EngagementResponse cancel(Utilisateur current, Long id) {
        Engagement e = fetchOwned(current, id);
        if (e.getStatus() == EngagementStatus.IN_PROGRESS || e.getStatus() == EngagementStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel after work started");
        }
        e.setStatus(EngagementStatus.CANCELLED);
        return toResponse(e);
    }

    // ---- Helpers ----
    private Engagement fetchOwned(Utilisateur current, Long id) {
        if (current instanceof Client c) {
            return engagementRepository.findByIdAndClient_Id(id, c.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Engagement not found"));
        } else if (current instanceof Artisan a) {
            return engagementRepository.findByIdAndArtisan_Id(id, a.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Engagement not found"));
        }
        throw new IllegalStateException("Only CLIENT or ARTISAN can access engagements");
    }

    private EngagementResponse toResponse(Engagement e) {
        return new EngagementResponse(
                e.getId(),
                e.getRequest().getId(),
                e.getQuote().getId(),
                e.getClient().getId(),
                e.getArtisan().getId(),
                e.getAgreedPrice(),
                e.getStartDate(),
                e.getEndDate(),
                e.getStatus(),
                e.getCreatedAt()
        );
    }
}
