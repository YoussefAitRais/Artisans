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
 * Service for managing quote operations between artisans and clients.
 * 
 * Handles the complete quote lifecycle including creation, updates, acceptance,
 * and rejection. Manages business rules for quote eligibility, duplicate prevention,
 * and engagement creation upon quote acceptance.
 * 
 * @author Artisan Platform Team
 * @version 1.0
 */
@Service
public class QuoteService {

    // ============ DEPENDENCIES ============
    
    private final QuoteRepository quoteRepository;
    private final ServiceRequestRepository serviceRequestRepository;
    private final EngagementRepository engagementRepository;

    /**
     * Constructor for dependency injection.
     * 
     * @param quoteRepository repository for quote data access
     * @param serviceRequestRepository repository for service request data access
     * @param engagementRepository repository for engagement data access
     */
    public QuoteService(QuoteRepository quoteRepository,
                        ServiceRequestRepository serviceRequestRepository,
                        EngagementRepository engagementRepository) {
        this.quoteRepository = quoteRepository;
        this.serviceRequestRepository = serviceRequestRepository;
        this.engagementRepository = engagementRepository;
    }

    // ============ ARTISAN OPERATIONS ============

    /**
     * Creates a new quote for a service request as an artisan.
     * 
     * @param artisan the artisan creating the quote
     * @param requestId the service request ID to quote for
     * @param createRequest the quote creation data
     * @return the created quote response
     * @throws IllegalArgumentException if request not found
     * @throws IllegalStateException if request not accepting quotes or duplicate quote exists
     */
    @Transactional
    public QuoteResponse createQuoteAsArtisan(Artisan artisan, Long requestId, QuoteCreateRequest createRequest) {
        ServiceRequest serviceRequest = findServiceRequestById(requestId);
        
        validateQuoteCreation(serviceRequest, artisan, requestId);
        
        Quote quote = buildNewQuote(serviceRequest, artisan, createRequest);
        Quote savedQuote = quoteRepository.save(quote);
        
        updateServiceRequestStatus(serviceRequest);
        
        return mapToResponse(savedQuote);
    }

    /**
     * Updates an existing quote as an artisan.
     * 
     * @param artisan the artisan updating the quote
     * @param quoteId the quote ID to update
     * @param updateRequest the quote update data
     * @return the updated quote response
     * @throws IllegalArgumentException if quote not found
     * @throws IllegalStateException if quote cannot be updated
     */
    @Transactional
    public QuoteResponse updateQuoteAsArtisan(Artisan artisan, Long quoteId, QuoteUpdateRequest updateRequest) {
        Quote quote = findQuoteByIdAndArtisan(quoteId, artisan.getId());
        
        validateQuoteUpdate(quote);
        
        applyQuoteUpdates(quote, updateRequest);
        
        return mapToResponse(quote);
    }

    /**
     * Deletes a quote as an artisan.
     * 
     * @param artisan the artisan deleting the quote
     * @param quoteId the quote ID to delete
     * @throws IllegalArgumentException if quote not found
     * @throws IllegalStateException if quote cannot be deleted
     */
    @Transactional
    public void deleteQuoteAsArtisan(Artisan artisan, Long quoteId) {
        Quote quote = findQuoteByIdAndArtisan(quoteId, artisan.getId());
        
        validateQuoteDeletion(quote);
        
        quoteRepository.delete(quote);
    }

    /**
     * Retrieves all quotes created by an artisan.
     * 
     * @param artisan the artisan requesting their quotes
     * @param pageable pagination parameters
     * @return paginated list of artisan's quotes
     */
    @Transactional(readOnly = true)
    public Page<QuoteResponse> getArtisanQuotes(Artisan artisan, Pageable pageable) {
        return quoteRepository.findByArtisan_Id(artisan.getId(), pageable)
                .map(this::mapToResponse);
    }

    // ============ CLIENT OPERATIONS ============

    /**
     * Retrieves all quotes for a client's service request.
     * 
     * @param client the client requesting quotes
     * @param requestId the service request ID
     * @param pageable pagination parameters
     * @return paginated list of quotes for the request
     * @throws IllegalArgumentException if request not found or not owned by client
     */
    @Transactional(readOnly = true)
    public Page<QuoteResponse> getQuotesForClientRequest(Client client, Long requestId, Pageable pageable) {
        ServiceRequest serviceRequest = findServiceRequestByIdAndClient(requestId, client.getId());
        return quoteRepository.findByRequest_Id(serviceRequest.getId(), pageable)
                .map(this::mapToResponse);
    }

    /**
     * Accepts a quote, creating an engagement and rejecting other quotes.
     * 
     * @param client the client accepting the quote
     * @param quoteId the quote ID to accept
     * @return the accepted quote response
     * @throws IllegalArgumentException if quote not found
     * @throws IllegalStateException if quote cannot be accepted
     */
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

    /**
     * Retrieves all quotes for a specific request (admin only).
     * 
     * @param requestId the service request ID
     * @param pageable pagination parameters
     * @return paginated list of quotes for the request
     */
    @Transactional(readOnly = true)
    public Page<QuoteResponse> getQuotesByRequestForAdmin(Long requestId, Pageable pageable) {
        return quoteRepository.findByRequest_Id(requestId, pageable)
                .map(this::mapToResponse);
    }

    /**
     * Retrieves all quotes by a specific artisan (admin only).
     * 
     * @param artisanId the artisan ID
     * @param pageable pagination parameters
     * @return paginated list of quotes by the artisan
     */
    @Transactional(readOnly = true)
    public Page<QuoteResponse> getQuotesByArtisanForAdmin(Long artisanId, Pageable pageable) {
        return quoteRepository.findByArtisan_Id(artisanId, pageable)
                .map(this::mapToResponse);
    }

    /**
     * Deletes a quote (admin only).
     * 
     * @param quoteId the quote ID to delete
     * @throws IllegalArgumentException if quote not found
     */
    @Transactional
    public void deleteQuoteAsAdmin(Long quoteId) {
        if (!quoteRepository.existsById(quoteId)) {
            throw new IllegalArgumentException("Quote not found with ID: " + quoteId);
        }
        quoteRepository.deleteById(quoteId);
    }

    // ============ PRIVATE HELPER METHODS ============
    
    /**
     * Finds a service request by ID or throws an exception.
     * 
     * @param requestId the service request ID
     * @return the found service request
     * @throws IllegalArgumentException if not found
     */
    private ServiceRequest findServiceRequestById(Long requestId) {
        return serviceRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Service request not found with ID: " + requestId));
    }
    
    /**
     * Finds a service request by ID and client ID or throws an exception.
     * 
     * @param requestId the service request ID
     * @param clientId the client ID
     * @return the found service request
     * @throws IllegalArgumentException if not found
     */
    private ServiceRequest findServiceRequestByIdAndClient(Long requestId, Long clientId) {
        return serviceRequestRepository.findByIdAndClient_Id(requestId, clientId)
                .orElseThrow(() -> new IllegalArgumentException("Service request not found for this client"));
    }
    
    /**
     * Finds a quote by ID or throws an exception.
     * 
     * @param quoteId the quote ID
     * @return the found quote
     * @throws IllegalArgumentException if not found
     */
    private Quote findQuoteById(Long quoteId) {
        return quoteRepository.findById(quoteId)
                .orElseThrow(() -> new IllegalArgumentException("Quote not found with ID: " + quoteId));
    }
    
    /**
     * Finds a quote by ID and artisan ID or throws an exception.
     * 
     * @param quoteId the quote ID
     * @param artisanId the artisan ID
     * @return the found quote
     * @throws IllegalArgumentException if not found
     */
    private Quote findQuoteByIdAndArtisan(Long quoteId, Long artisanId) {
        return quoteRepository.findByIdAndArtisan_Id(quoteId, artisanId)
                .orElseThrow(() -> new IllegalArgumentException("Quote not found for this artisan"));
    }
    
    /**
     * Validates that a quote can be created for the given request.
     * 
     * @param serviceRequest the service request
     * @param artisan the artisan creating the quote
     * @param requestId the request ID
     * @throws IllegalStateException if quote creation is not allowed
     */
    private void validateQuoteCreation(ServiceRequest serviceRequest, Artisan artisan, Long requestId) {
        if (!isRequestAcceptingQuotes(serviceRequest)) {
            throw new IllegalStateException("Request is not accepting quotes");
        }
        
        if (quoteRepository.existsByRequest_IdAndArtisan_Id(requestId, artisan.getId())) {
            throw new IllegalStateException("You already sent a quote for this request");
        }
    }
    
    /**
     * Checks if a service request is currently accepting quotes.
     * 
     * @param serviceRequest the service request to check
     * @return true if accepting quotes, false otherwise
     */
    private boolean isRequestAcceptingQuotes(ServiceRequest serviceRequest) {
        RequestStatus status = serviceRequest.getStatus();
        return status == RequestStatus.PENDING || status == RequestStatus.RESPONDED;
    }
    
    /**
     * Builds a new quote entity from the request data.
     * 
     * @param serviceRequest the service request
     * @param artisan the artisan creating the quote
     * @param createRequest the quote creation data
     * @return the new quote entity
     */
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
    
    /**
     * Updates the service request status when the first quote is received.
     * 
     * @param serviceRequest the service request to update
     */
    private void updateServiceRequestStatus(ServiceRequest serviceRequest) {
        if (serviceRequest.getStatus() == RequestStatus.PENDING) {
            serviceRequest.setStatus(RequestStatus.RESPONDED);
        }
    }
    
    /**
     * Validates that a quote can be updated.
     * 
     * @param quote the quote to validate
     * @throws IllegalStateException if quote cannot be updated
     */
    private void validateQuoteUpdate(Quote quote) {
        if (quote.getStatus() != QuoteStatus.SENT) {
            throw new IllegalStateException("Only SENT quotes can be updated");
        }
    }
    
    /**
     * Applies updates to a quote entity.
     * 
     * @param quote the quote to update
     * @param updateRequest the update data
     */
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
    
    /**
     * Validates that a quote can be deleted.
     * 
     * @param quote the quote to validate
     * @throws IllegalStateException if quote cannot be deleted
     */
    private void validateQuoteDeletion(Quote quote) {
        if (quote.getStatus() != QuoteStatus.SENT) {
            throw new IllegalStateException("Only SENT quotes can be deleted");
        }
    }
    
    /**
     * Validates that a quote can be accepted by the client.
     * 
     * @param client the client accepting the quote
     * @param serviceRequest the service request
     * @param quote the quote to accept
     * @throws IllegalStateException if quote cannot be accepted
     */
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
    
    /**
     * Checks if a service request is closed to new quotes.
     * 
     * @param serviceRequest the service request to check
     * @return true if closed, false otherwise
     */
    private boolean isRequestClosed(ServiceRequest serviceRequest) {
        RequestStatus status = serviceRequest.getStatus();
        return status == RequestStatus.ACCEPTED || 
               status == RequestStatus.CANCELLED || 
               status == RequestStatus.COMPLETED;
    }
    
    /**
     * Processes quote acceptance by updating all quotes and request status.
     * 
     * @param acceptedQuote the quote being accepted
     * @param serviceRequest the service request
     */
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
    
    /**
     * Creates an engagement if one doesn't already exist for the request.
     * 
     * @param quote the accepted quote
     * @param serviceRequest the service request
     */
    private void createEngagementIfNeeded(Quote quote, ServiceRequest serviceRequest) {
        if (!engagementRepository.existsByRequest_Id(serviceRequest.getId())) {
            Engagement engagement = buildEngagement(quote, serviceRequest);
            engagementRepository.save(engagement);
        }
    }
    
    /**
     * Builds a new engagement entity from the accepted quote.
     * 
     * @param quote the accepted quote
     * @param serviceRequest the service request
     * @return the new engagement entity
     */
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

    /**
     * Maps a Quote entity to a QuoteResponse DTO.
     * 
     * @param quote the quote entity
     * @return the response DTO
     */
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
