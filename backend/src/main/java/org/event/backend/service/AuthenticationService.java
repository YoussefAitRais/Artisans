package org.event.backend.service;

import org.event.backend.dto.AuthResponse;
import org.event.backend.dto.LoginRequest;
import org.event.backend.dto.RegisterRequest;
import org.event.backend.dto.artisan.RegisterArtisanRequest;
import org.event.backend.entity.*;
import org.event.backend.repository.CategoryRepository;
import org.event.backend.repository.UtilisateurRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final UtilisateurRepository utilisateurRepository;
    private final CategoryRepository categoryRepository;   // <-- NEW
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationService(UtilisateurRepository utilisateurRepository,
                                 CategoryRepository categoryRepository,       // <-- NEW
                                 PasswordEncoder passwordEncoder,
                                 JwtService jwtService,
                                 AuthenticationManager authenticationManager) {
        this.utilisateurRepository = utilisateurRepository;
        this.categoryRepository = categoryRepository;      // <-- NEW
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    // -------- Register Client ----------
    public AuthResponse registerClient(RegisterRequest request) {
        if (utilisateurRepository.existsByEmail(request.getEmail())) {
            throw new DataIntegrityViolationException("Email already exists");
        }

        Client client = new Client(
                request.getNom().trim(),
                request.getPrenom().trim(),
                request.getEmail().trim(),
                passwordEncoder.encode(request.getPassword()),
                Role.CLIENT
        );
        utilisateurRepository.save(client);

        String token = jwtService.generateToken(client);

        return new AuthResponse(
                token,
                client.getId(),
                client.getEmail(),
                client.getRole().name(),
                client.getNom(),
                client.getPrenom()
        );
    }

    // -------- Register Artisan ----------
    public AuthResponse registerArtisan(RegisterArtisanRequest req) {
        if (utilisateurRepository.existsByEmail(req.getEmail())) {
            throw new DataIntegrityViolationException("Email already exists");
        }

        // categoryId is required (because Artisan.category nullable=false)
        Category cat = categoryRepository.findById(req.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        Artisan artisan = new Artisan(
                req.getNom().trim(),
                req.getPrenom().trim(),
                req.getEmail().trim(),
                passwordEncoder.encode(req.getPassword()),
                Role.ARTISAN,
                req.getMetier().trim(),
                req.getLocalisation() == null ? null : req.getLocalisation().trim(),
                req.getDescription() == null ? null : req.getDescription().trim()
        );
        artisan.setCategory(cat); // <-- لازم

        utilisateurRepository.save(artisan);

        String token = jwtService.generateToken(artisan);

        return new AuthResponse(
                token,
                artisan.getId(),
                artisan.getEmail(),
                artisan.getRole().name(),
                artisan.getNom(),
                artisan.getPrenom()
        );
    }

    // -------- Login ----------
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        Utilisateur utilisateur = utilisateurRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtService.generateToken(utilisateur);

        return new AuthResponse(
                token,
                utilisateur.getId(),
                utilisateur.getEmail(),
                utilisateur.getRole().name(),
                utilisateur.getNom(),
                utilisateur.getPrenom()
        );
    }
}
