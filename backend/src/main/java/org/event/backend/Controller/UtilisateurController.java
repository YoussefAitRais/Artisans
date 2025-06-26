package org.event.backend.Controller;


import org.event.backend.Entity.Utilisateur;
import org.event.backend.Security.UtilisateurService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/utilisateur")
public class UtilisateurController {

    private UtilisateurService utilisateurService;
    public UtilisateurController(UtilisateurService utilisateurService) {
        this.utilisateurService = utilisateurService;
    }

    @PostMapping("/Register")
    public ResponseEntity Register(@RequestBody Utilisateur utilisateur) {
        Utilisateur saved = utilisateurService.save(utilisateur);
        return ResponseEntity.ok().body(saved);
    }

    @GetMapping
    public ResponseEntity<List<Utilisateur>> GetAll() {
        return ResponseEntity.ok(utilisateurService.getAll());
    }

    @GetMapping("/email")
    public ResponseEntity GetUtilisateurByEmail(@RequestParam String email) {
        Optional<Utilisateur> utilisateur = utilisateurService.findByEmail(email);
        if (utilisateur.isPresent()) {
            return ResponseEntity.ok().body(utilisateur.get());
        }else
            return ResponseEntity.notFound().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity GetUtilisateurById(@PathVariable int id) {
        Optional<Utilisateur> utilisateur = utilisateurService.getById((long) id);
        if (utilisateur.isPresent()) {
            return ResponseEntity.ok().body(utilisateur.get());
        }else
            return ResponseEntity.notFound().build();
    }

    @PutMapping
    public ResponseEntity UpdateUtilisateur(@PathVariable Long id ,  @RequestBody Utilisateur utilisateur) {
        Optional<Utilisateur> updated = utilisateurService.update(id, utilisateur);
        return updated.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        boolean deleted = utilisateurService.delete(id);
        if (deleted) return ResponseEntity.noContent().build();
        else return ResponseEntity.notFound().build();
    }





}
