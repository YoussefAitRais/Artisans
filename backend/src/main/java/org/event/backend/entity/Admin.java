package org.event.backend.entity;

import jakarta.persistence.Entity;


@Entity
public class Admin extends Utilisateur {


    public Admin() {
        super();
    }


    public Admin(String nom, String prenom, String email, String passwordHash, Role role) {
        super(nom, prenom, email, passwordHash, role);
    }
}