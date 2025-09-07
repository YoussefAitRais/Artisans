package org.event.backend.service.inbox;

import org.event.backend.dto.inbox.ArtisanInboxItemResponse;
import org.event.backend.entity.Artisan;
import org.event.backend.entity.ServiceRequest;
import org.event.backend.repository.QuoteRepository;
import org.event.backend.repository.ServiceRequestRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ArtisanInboxService {

    private final ServiceRequestRepository requestRepo;
    private final QuoteRepository quoteRepo;

    public ArtisanInboxService(ServiceRequestRepository r, QuoteRepository q) {
        this.requestRepo = r; this.quoteRepo = q;
    }

    public Page<ArtisanInboxItemResponse> listInbox(Artisan artisan,
                                                    Pageable pageable,
                                                    String status, String q) {
        Long catId = artisan.getCategory().getId();

        Page<ServiceRequest> page = requestRepo.findByCategory_IdAndEngagementIsNull(catId, pageable);

        List<ArtisanInboxItemResponse> mapped = page.getContent().stream()
                .map(sr -> {
                    boolean responded = quoteRepo.existsByRequest_IdAndArtisan_Id(sr.getId(), artisan.getId());
                    String s = responded ? "RESPONDED" : "PENDING";
                    return new ArtisanInboxItemResponse(
                            sr.getId(),
                            sr.getTitle(),
                            sr.getCity(),
                            sr.getDescription(),
                            sr.getDesiredDate(),
                            sr.getCreatedAt(),
                            s,
                            sr.getCategory() != null ? sr.getCategory().getId() : null
                    );
                })
                .collect(Collectors.toList());

        List<ArtisanInboxItemResponse> filtered = mapped.stream()
                .filter(item -> {
                    boolean statusOk = (status == null || status.isBlank() || "ALL".equalsIgnoreCase(status))
                            || item.getStatus().equalsIgnoreCase(status);
                    boolean qOk = (q == null || q.isBlank())
                            || contains(item.getTitle(), q)
                            || contains(item.getCity(), q)
                            || contains(item.getDescription(), q);
                    return statusOk && qOk;
                })
                .collect(Collectors.toList());


        return new PageImpl<>(filtered, pageable, page.getTotalElements());
    }

    private boolean contains(String s, String q) {
        if (s == null) return false;
        return s.toLowerCase().contains(q.toLowerCase().trim());
    }
}
