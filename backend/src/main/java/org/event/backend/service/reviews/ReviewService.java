package org.event.backend.service.reviews;

import org.event.backend.dto.review.ReviewCreateRequest;
import org.event.backend.dto.review.ReviewResponse;
import org.event.backend.dto.review.ReviewUpdateRequest;
import org.event.backend.entity.*;
import org.event.backend.repository.ArtisanRepository;
import org.event.backend.repository.ReviewRepository;
import org.event.backend.repository.ServiceRequestRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Business rules for reviews.
 */
@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ArtisanRepository artisanRepository;
    private final ServiceRequestRepository serviceRequestRepository;

    public ReviewService(ReviewRepository reviewRepository,
                         ArtisanRepository artisanRepository,
                         ServiceRequestRepository serviceRequestRepository) {
        this.reviewRepository = reviewRepository;
        this.artisanRepository = artisanRepository;
        this.serviceRequestRepository = serviceRequestRepository;
    }

    // ------- Public for artisan profile -------
    @Transactional(readOnly = true)
    public Page<ReviewResponse> getReviewsForArtisan(Long artisanId, Pageable pageable) {
        return reviewRepository.findByArtisan_Id(artisanId, pageable).map(this::toResponse);
    }

    // ------- Client (owner) -------
    @Transactional(readOnly = true)
    public Page<ReviewResponse> myReviews(Client client, Pageable pageable) {
        return reviewRepository.findByClient_Id(client.getId(), pageable).map(this::toResponse);
    }

    @Transactional
    public ReviewResponse create(Client client, ReviewCreateRequest req) {
        Artisan artisan = artisanRepository.findById(req.getArtisanId())
                .orElseThrow(() -> new IllegalArgumentException("Artisan not found"));

        Review review = new Review();
        review.setClient(client);
        review.setArtisan(artisan);
        review.setRating(req.getRating());
        review.setComment(req.getComment());

        // Optional linkage & policy: allow review only if request is COMPLETED
        if (req.getRequestId() != null) {
            ServiceRequest sr = serviceRequestRepository.findById(req.getRequestId())
                    .orElseThrow(() -> new IllegalArgumentException("Request not found"));

            // owner check
            if (!sr.getClient().getId().equals(client.getId())) {
                throw new IllegalStateException("You can only review your own request");
            }
            // status policy
            if (sr.getStatus() != RequestStatus.COMPLETED) {
                throw new IllegalStateException("Only COMPLETED requests can be reviewed");
            }
            // one review per client per request (service-level check)
            if (reviewRepository.existsByClient_IdAndRequest_Id(client.getId(), sr.getId())) {
                throw new IllegalStateException("You already reviewed this request");
            }
            review.setRequest(sr);
        }

        Review saved = reviewRepository.save(review);
        return toResponse(saved);
    }

    @Transactional
    public ReviewResponse update(Client client, Long reviewId, ReviewUpdateRequest req) {
        Review r = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));

        if (!r.getClient().getId().equals(client.getId())) {
            throw new IllegalStateException("You can only update your own review");
        }

        r.setRating(req.getRating());
        r.setComment(req.getComment());
        return toResponse(r);
    }

    @Transactional
    public void delete(Client client, Long reviewId) {
        Review r = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));

        if (!r.getClient().getId().equals(client.getId())) {
            throw new IllegalStateException("You can only delete your own review");
        }
        reviewRepository.delete(r);
    }

    // ------- Admin -------
    @Transactional(readOnly = true)
    public Page<ReviewResponse> adminList(Pageable pageable) {
        return reviewRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional
    public void adminDelete(Long id) {
        if (!reviewRepository.existsById(id)) {
            throw new IllegalArgumentException("Review not found");
        }
        reviewRepository.deleteById(id);
    }

    // ------- Helpers -------
    private ReviewResponse toResponse(Review r) {
        Long reqId = (r.getRequest() != null) ? r.getRequest().getId() : null;
        return new ReviewResponse(
                r.getId(),
                r.getArtisan().getId(),
                r.getClient().getId(),
                reqId,
                r.getRating(),
                r.getComment(),
                r.getCreatedAt()
        );
    }
}
