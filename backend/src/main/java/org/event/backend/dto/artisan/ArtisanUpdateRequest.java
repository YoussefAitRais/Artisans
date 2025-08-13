package org.event.backend.dto.artisan;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Payload for updating an artisan profile (self-update or admin).
 */
public class ArtisanUpdateRequest {

    @NotBlank(message = "First name is required")
    @Size(max = 120)
    private String nom;

    @Size(max = 120)
    private String prenom;

    @NotBlank(message = "Metier is required")
    @Size(max = 120)
    private String metier;

    @Size(max = 120)
    private String localisation;

    @Size(max = 2000)
    private String description;

    public ArtisanUpdateRequest() {}

    public ArtisanUpdateRequest(String nom, String prenom, String metier, String localisation, String description) {
        this.nom = nom;
        this.prenom = prenom;
        this.metier = metier;
        this.localisation = localisation;
        this.description = description;
    }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public String getMetier() { return metier; }
    public void setMetier(String metier) { this.metier = metier; }
    public String getLocalisation() { return localisation; }
    public void setLocalisation(String localisation) { this.localisation = localisation; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
