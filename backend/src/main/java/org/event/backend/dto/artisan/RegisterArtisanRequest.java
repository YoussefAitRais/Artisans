package org.event.backend.dto.artisan;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class RegisterArtisanRequest {

    @NotBlank @Size(max = 120)
    private String nom;

    @NotBlank @Size(max = 120)
    private String prenom;

    @NotBlank @Email
    private String email;

    @NotBlank @Size(min = 6, max = 100)
    private String password;

    @NotBlank @Size(max = 120)
    private String metier;

    @Size(max = 120)
    private String localisation;

    @Size(max = 2000)
    private String description;

    @NotNull
    private Long categoryId;




    public RegisterArtisanRequest() {}

    // getters & setters
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getMetier() { return metier; }
    public void setMetier(String metier) { this.metier = metier; }
    public String getLocalisation() { return localisation; }
    public void setLocalisation(String localisation) { this.localisation = localisation; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }


    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

}
