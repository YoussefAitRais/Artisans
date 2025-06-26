package org.event.backend.Security;


import org.event.backend.Entity.Utilisateur;
import org.event.backend.repository.UtilisateurRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UtilisateurService {

    private UtilisateurRepository utilisateurRepository;
    public UtilisateurService(UtilisateurRepository utilisateurRepository) {
        this.utilisateurRepository = utilisateurRepository;

    }

    public Optional<Utilisateur> findByEmail(String email) {
        return utilisateurRepository.findByEmail(email);
    }

    public Utilisateur save(Utilisateur utilisateur) {
        return utilisateurRepository.save(utilisateur);
    }


    public Optional<Utilisateur> getById(Long id) {
        return utilisateurRepository.findById(id);
    }


    public List<Utilisateur> getAll() {
        return utilisateurRepository.findAll();
    }

    public Optional<Utilisateur> update(Long id, Utilisateur updatedUtilisateur) {
        return utilisateurRepository.findById(id).map(existing -> {
            existing.setNom(updatedUtilisateur.getNom());
            existing.setEmail(updatedUtilisateur.getEmail());
            existing.setPassword(updatedUtilisateur.getPassword());
            existing.setRole(updatedUtilisateur.getRole());
            return utilisateurRepository.save(existing);
        });
    }

    public boolean delete(Long id) {
        if (utilisateurRepository.existsById(id)) {
            utilisateurRepository.deleteById(id);
            return true;
        }
        return false;
    }

}
