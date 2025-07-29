package org.event.backend.dto;

public class AuthResponse {
    private String token;
    private String email;
    private String role;
    private String nom;
    private String prenom;

    public AuthResponse() {}

    public AuthResponse(String token, String email, String role, String nom, String prenom) {
        this.token = token;
        this.email = email;
        this.role = role;
        this.nom = nom;
        this.prenom = prenom;
    }

    // Getters et Setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
}
