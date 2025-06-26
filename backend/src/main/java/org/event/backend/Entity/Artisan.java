package org.event.backend.Entity;


import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Artisan extends Utilisateur{


    private String metier;
    private String localisation;
    private String description;


}
