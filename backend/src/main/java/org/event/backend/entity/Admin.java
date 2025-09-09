package org.event.backend.entity;

import jakarta.persistence.Entity;

/**
 * Admin entity for system administrators
 * 
 * Clean Code for Beginners:
 * - Simple entity extending Utilisateur base class
 * - No additional fields needed for basic admin functionality
 * - Can be extended later with admin-specific properties if needed
 */
@Entity
public class Admin extends Utilisateur {

    /**
     * Default constructor required by JPA
     */
    public Admin() {
        super();
    }

    /**
     * Constructor for creating admin users
     * 
     * @param nom - Last name of the administrator
     * @param prenom - First name of the administrator
     * @param email - Email address (used for login)
     * @param passwordHash - Encrypted password
     * @param role - Should always be Role.ADMIN for this entity
     */
    public Admin(String nom, String prenom, String email, String passwordHash, Role role) {
        super(nom, prenom, email, passwordHash, role);
    }
}