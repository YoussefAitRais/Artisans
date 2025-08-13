package org.event.backend.service;

import org.event.backend.dto.AuthResponse;
import org.event.backend.dto.LoginRequest;
import org.event.backend.dto.RegisterRequest;
import org.event.backend.entity.Artisan;
import org.event.backend.entity.Client;
import org.event.backend.entity.Role;
import org.event.backend.entity.Utilisateur;
import org.event.backend.repository.UtilisateurRepository;
import org.event.backend.service.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationService(UtilisateurRepository utilisateurRepository,
                                 PasswordEncoder passwordEncoder,
                                 JwtService jwtService,
                                 AuthenticationManager authenticationManager) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    /**
     * Register a new client
     */
    public AuthResponse registerClient(RegisterRequest request) {

        if (utilisateurRepository.existsByEmail(request.getEmail())){
            throw new RuntimeException( "Email Already Exist");
        }

        Client client = new Client(
                request.getNom(),
                request.getPrenom(),
                request.getEmail(),
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

    /**
     * Register a new artisan
     */
    public AuthResponse registerArtisan(RegisterRequest request) {
        if (utilisateurRepository.existsByEmail(request.getEmail())){
            throw new RuntimeException( "Email Already Exist");
        }
        Artisan artisan = new Artisan(
                request.getNom(),
                request.getPrenom(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                Role.ARTISAN,
                null, // metier
                null, // localisation
                null  // description
        );
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

    /**
     * Authenticate existing user
     */
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
