package org.event.backend.dto.client;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Payload for updating a client profile (self-update or admin).
 */
public class ClientUpdateRequest {

    @NotBlank(message = "First name (nom) is required")
    @Size(max = 120)
    private String nom;

    @Size(max = 120)
    private String prenom;

    @Size(max = 30)
    @Pattern(regexp = "^[0-9+\\-()\\s]*$", message = "Telephone contains invalid characters")
    private String telephone;

    public ClientUpdateRequest() {}

    public ClientUpdateRequest(String nom, String prenom, String telephone) {
        this.nom = nom;
        this.prenom = prenom;
        this.telephone = telephone;
    }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
}
