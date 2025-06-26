package org.event.backend.Entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor


public class Utilisateur {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column (nullable = false)
    private String nom;
    private String prenom;


    @Column (unique = true, nullable = false)
    private String email;

    @Column (nullable = false)
    private String password;


    @Enumerated( EnumType.STRING)
    private Role role;



}
