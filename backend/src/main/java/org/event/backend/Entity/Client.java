package org.event.backend.Entity;


import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor


@PrimaryKeyJoinColumn(name = "id")
public class Client extends Utilisateur {



    private String telephone;

}
