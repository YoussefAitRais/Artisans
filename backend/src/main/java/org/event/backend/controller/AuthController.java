package org.event.backend.controller;

import jakarta.validation.Valid;
import org.event.backend.dto.AuthResponse;
import org.event.backend.dto.LoginRequest;
import org.event.backend.dto.RegisterRequest;
import org.event.backend.service.AuthenticationService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:4200"}, allowCredentials = "true")
public class AuthController {

    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping(path = "/register-client", consumes = "application/json", produces = "application/json")
    public ResponseEntity<AuthResponse> registerClient(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authenticationService.registerClient(request);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + response.getToken());
        headers.add("Access-Control-Expose-Headers", "Authorization");

        return ResponseEntity.status(HttpStatus.CREATED).headers(headers).body(response);
    }

    @PostMapping(path = "/register-artisan", consumes = "application/json", produces = "application/json")
    public ResponseEntity<AuthResponse> registerArtisan(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authenticationService.registerArtisan(request);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + response.getToken());
        headers.add("Access-Control-Expose-Headers", "Authorization");

        return ResponseEntity.status(HttpStatus.CREATED).headers(headers).body(response);
    }

    @PostMapping(path = "/login", consumes = "application/json", produces = "application/json")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authenticationService.login(request);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + response.getToken());
        headers.add("Access-Control-Expose-Headers", "Authorization");

        return ResponseEntity.ok().headers(headers).body(response);
    }
}
