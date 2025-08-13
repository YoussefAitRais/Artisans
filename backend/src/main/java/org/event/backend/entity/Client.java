package org.event.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class Client extends Utilisateur {

    @Column(length = 30)
    private String telephone;

    public Client() {}

    public Client(String nom,
                  String prenom,
                  String email,
                  String passwordHash,
                  Role role
                  ) {
        super(nom, prenom, email, passwordHash, role);

    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }
}
