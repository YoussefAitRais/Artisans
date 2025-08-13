package org.event.backend.dto.artisan;

/**
 * Response DTO representing an artisan profile.
 */
public class ArtisanResponse {

    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String metier;
    private String localisation;
    private String description;

    public ArtisanResponse() {}

    public ArtisanResponse(Long id, String nom, String prenom, String email,
                           String metier, String localisation, String description) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.metier = metier;
        this.localisation = localisation;
        this.description = description;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getMetier() { return metier; }
    public void setMetier(String metier) { this.metier = metier; }
    public String getLocalisation() { return localisation; }
    public void setLocalisation(String localisation) { this.localisation = localisation; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
