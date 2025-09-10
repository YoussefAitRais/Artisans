package org.event.backend.service.artisan;

import org.event.backend.dto.artisan.ArtisanResponse;
import org.event.backend.dto.artisan.ArtisanUpdateRequest;
import org.event.backend.entity.Artisan;
import org.event.backend.entity.Utilisateur;
import org.event.backend.repository.ArtisanRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

/**
 * Service layer for managing artisan profiles and operations.
 * 
 * This service provides comprehensive artisan management including:
 * - Public search functionality with filtering
 * - Profile retrieval and updates
 * - Administrative operations
 * 
 * @author Artisan Platform Team
 * @version 1.0
 */
@Service
public class ArtisanService {

    private final ArtisanRepository artisanRepository;

    /**
     * Constructor for dependency injection.
     * 
     * @param artisanRepository the repository for artisan data access
     */
    public ArtisanService(ArtisanRepository artisanRepository) {
        this.artisanRepository = artisanRepository;
    }

    // ============ PUBLIC OPERATIONS ============
    
    /**
     * Searches for artisans with optional filtering criteria.
     * 
     * @param metier the profession/trade to filter by (optional)
     * @param localisation the location to filter by (optional)
     * @param q the general search query (searches name, description, profession)
     * @param pageable pagination parameters
     * @return paginated list of matching artisans
     */
    @Transactional(readOnly = true)
    public Page<ArtisanResponse> searchArtisans(String metier, String localisation, String q, Pageable pageable) {
        Specification<Artisan> searchCriteria = buildSearchSpecification(metier, localisation, q);
        return artisanRepository.findAll(searchCriteria, pageable)
                .map(this::mapToResponse);
    }

    /**
     * Retrieves an artisan profile by ID.
     * 
     * @param id the artisan's unique identifier
     * @return the artisan's profile information
     * @throws IllegalArgumentException if artisan not found
     */
    @Transactional(readOnly = true)
    public ArtisanResponse getArtisanById(Long id) {
        Artisan artisan = findArtisanById(id);
        return mapToResponse(artisan);
    }

    // ============ ARTISAN SELF-SERVICE OPERATIONS ============
    
    /**
     * Retrieves the current artisan's own profile.
     * 
     * @param currentUser the authenticated artisan user
     * @return the artisan's profile information
     * @throws IllegalArgumentException if artisan profile not found
     */
    @Transactional(readOnly = true)
    public ArtisanResponse getCurrentArtisanProfile(Utilisateur currentUser) {
        Artisan artisan = findArtisanById(currentUser.getId());
        return mapToResponse(artisan);
    }

    /**
     * Updates the current artisan's profile information.
     * 
     * @param currentUser the authenticated artisan user
     * @param updateRequest the profile update data
     * @return the updated artisan profile
     * @throws IllegalArgumentException if artisan profile not found
     */
    @Transactional
    public ArtisanResponse updateCurrentArtisanProfile(Utilisateur currentUser, ArtisanUpdateRequest updateRequest) {
        Artisan artisan = findArtisanById(currentUser.getId());
        
        updateArtisanFields(artisan, updateRequest);
        
        artisanRepository.save(artisan);
        return mapToResponse(artisan);
    }

    // ============ ADMINISTRATIVE OPERATIONS ============
    
    /**
     * Retrieves all artisans for administrative purposes.
     * 
     * @param pageable pagination parameters
     * @return paginated list of all artisans
     */
    @Transactional(readOnly = true)
    public Page<ArtisanResponse> getAllArtisansForAdmin(Pageable pageable) {
        return artisanRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    /**
     * Retrieves a specific artisan for administrative purposes.
     * 
     * @param id the artisan's unique identifier
     * @return the artisan's profile information
     * @throws IllegalArgumentException if artisan not found
     */
    @Transactional(readOnly = true)
    public ArtisanResponse getArtisanForAdmin(Long id) {
        Artisan artisan = findArtisanById(id);
        return mapToResponse(artisan);
    }

    /**
     * Updates an artisan's profile as an administrator.
     * 
     * @param id the artisan's unique identifier
     * @param updateRequest the profile update data
     * @return the updated artisan profile
     * @throws IllegalArgumentException if artisan not found
     */
    @Transactional
    public ArtisanResponse updateArtisanAsAdmin(Long id, ArtisanUpdateRequest updateRequest) {
        Artisan artisan = findArtisanById(id);
        
        updateArtisanFields(artisan, updateRequest);
        
        artisanRepository.save(artisan);
        return mapToResponse(artisan);
    }

    /**
     * Deletes an artisan profile as an administrator.
     * 
     * @param id the artisan's unique identifier
     * @throws IllegalArgumentException if artisan not found
     */
    @Transactional
    public void deleteArtisanAsAdmin(Long id) {
        if (!artisanRepository.existsById(id)) {
            throw new IllegalArgumentException("Artisan not found with ID: " + id);
        }
        artisanRepository.deleteById(id);
    }

    // ============ PRIVATE HELPER METHODS ============
    
    /**
     * Finds an artisan by ID or throws an exception if not found.
     * 
     * @param id the artisan's unique identifier
     * @return the found artisan entity
     * @throws IllegalArgumentException if artisan not found
     */
    private Artisan findArtisanById(Long id) {
        return artisanRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Artisan not found with ID: " + id));
    }
    
    /**
     * Updates artisan entity fields with values from the update request.
     * 
     * @param artisan the artisan entity to update
     * @param updateRequest the update data
     */
    private void updateArtisanFields(Artisan artisan, ArtisanUpdateRequest updateRequest) {
        artisan.setNom(cleanString(updateRequest.getNom()));
        artisan.setPrenom(cleanString(updateRequest.getPrenom()));
        artisan.setMetier(cleanString(updateRequest.getMetier()));
        artisan.setLocalisation(cleanString(updateRequest.getLocalisation()));
        artisan.setDescription(cleanString(updateRequest.getDescription()));
    }
    
    /**
     * Cleans and trims string input, handling null values.
     * 
     * @param input the string to clean
     * @return cleaned string or null if input was null
     */
    private String cleanString(String input) {
        return input != null ? input.trim() : null;
    }

    /**
     * Builds a JPA Specification for artisan search with multiple optional criteria.
     * 
     * @param metier profession/trade filter (optional)
     * @param localisation location filter (optional)
     * @param generalQuery general search query (optional)
     * @return JPA Specification for the search criteria
     */
    private Specification<Artisan> buildSearchSpecification(String metier, String localisation, String generalQuery) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Filter by profession if provided
            if (isNotBlank(metier)) {
                predicates.add(createLikePredicateIgnoreCase(criteriaBuilder, root.get("metier"), metier));
            }
            
            // Filter by location if provided
            if (isNotBlank(localisation)) {
                predicates.add(createLikePredicateIgnoreCase(criteriaBuilder, root.get("localisation"), localisation));
            }
            
            // General search across multiple fields if provided
            if (isNotBlank(generalQuery)) {
                predicates.add(buildGeneralSearchPredicate(criteriaBuilder, root, generalQuery));
            }

            return predicates.isEmpty() 
                ? criteriaBuilder.conjunction() 
                : criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
    
    /**
     * Builds a predicate for general search across multiple artisan fields.
     * 
     * @param criteriaBuilder JPA criteria builder
     * @param root the root entity
     * @param searchTerm the search term
     * @return combined OR predicate for multiple field search
     */
    private Predicate buildGeneralSearchPredicate(jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder, 
                                                   jakarta.persistence.criteria.Root<Artisan> root, 
                                                   String searchTerm) {
        String likePattern = "%" + searchTerm.toLowerCase().trim() + "%";
        
        return criteriaBuilder.or(
            criteriaBuilder.like(criteriaBuilder.lower(root.get("nom")), likePattern),
            criteriaBuilder.like(criteriaBuilder.lower(root.get("prenom")), likePattern),
            criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), likePattern),
            criteriaBuilder.like(criteriaBuilder.lower(root.get("metier")), likePattern)
        );
    }
    
    /**
     * Creates a case-insensitive LIKE predicate for a given field and value.
     * 
     * @param criteriaBuilder JPA criteria builder
     * @param field the field to search in
     * @param value the value to search for
     * @return LIKE predicate with case-insensitive matching
     */
    private Predicate createLikePredicateIgnoreCase(jakarta.persistence.criteria.CriteriaBuilder criteriaBuilder,
                                                     jakarta.persistence.criteria.Path<String> field,
                                                     String value) {
        String likePattern = "%" + value.toLowerCase().trim() + "%";
        return criteriaBuilder.like(criteriaBuilder.lower(field), likePattern);
    }
    
    /**
     * Checks if a string is not null and not blank.
     * 
     * @param str the string to check
     * @return true if string has meaningful content, false otherwise
     */
    private boolean isNotBlank(String str) {
        return str != null && !str.isBlank();
    }

    /**
     * Maps an Artisan entity to an ArtisanResponse DTO.
     * 
     * @param artisan the artisan entity
     * @return the response DTO
     */
    private ArtisanResponse mapToResponse(Artisan artisan) {
        return new ArtisanResponse(
                artisan.getId(),
                artisan.getNom(),
                artisan.getPrenom(),
                artisan.getEmail(),
                artisan.getMetier(),
                artisan.getLocalisation(),
                artisan.getDescription()
        );
    }
}
