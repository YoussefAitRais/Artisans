package org.event.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;


@Entity
public class Artisan extends Utilisateur {

    @Column(nullable = false, length = 120)
    private String metier;       // trade/specialty

    @Column(length = 120)
    private String localisation; // city/region

    @Column(length = 2000)
    private String description;  // profile bio/description

    public Artisan() {}

    public Artisan(String nom,
                   String prenom,
                   String email,
                   String passwordHash,
                   Role role,
                   String metier,
                   String localisation,
                   String description) {
        super(nom, prenom, email, passwordHash, role);
        this.metier = metier;
        this.localisation = localisation;
        this.description = description;
    }

    public String getMetier() { return metier; }

    public void setMetier(String metier) { this.metier = metier; }

    public String getLocalisation() { return localisation; }

    public void setLocalisation(String localisation) { this.localisation = localisation; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }
}
