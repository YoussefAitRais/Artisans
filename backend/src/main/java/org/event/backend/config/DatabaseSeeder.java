package org.event.backend.config;

import org.event.backend.entity.Admin;
import org.event.backend.entity.Role;
import org.event.backend.repository.UtilisateurRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseSeeder.class);
    private static final int MIN_PASSWORD_LENGTH = 6;
    
    // ============ DEPENDENCIES ============
    
    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    
    // ============ CONFIGURATION PROPERTIES ============
    
    @Value("${app.admin.email:admin@artisan.com}")
    private String adminEmail;
    
    @Value("${app.admin.password:admin123}")
    private String adminPassword;
    
    @Value("${app.admin.firstname:Admin}")
    private String adminFirstName;
    
    @Value("${app.admin.lastname:System}")
    private String adminLastName;
    
    @Value("${app.admin.seed.enabled:true}")
    private boolean seedingEnabled;

    // ============ CONSTRUCTOR ============
    
    public DatabaseSeeder(UtilisateurRepository utilisateurRepository, 
                         PasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ============ MAIN EXECUTION METHOD ============
    
    @Override
    public void run(String... args) {
        logger.info("Starting database seeding process...");
        
        if (!isSeedingEnabled()) {
            return;
        }
        
        if (!hasValidConfiguration()) {
            logger.error("Invalid admin configuration - seeding aborted");
            return;
        }
        
        try {
            createAdminUserIfNeeded();
            logger.info("Database seeding completed successfully");
        } catch (Exception e) {
            logger.error("Database seeding failed: {}", e.getMessage(), e);
            // Continue application startup even if seeding fails
        }
    }

    // ============ PRIVATE HELPER METHODS ============
    
    private boolean isSeedingEnabled() {
        if (!seedingEnabled) {
            logger.info("Admin seeding is disabled in configuration");
            return false;
        }
        return true;
    }
    
    private boolean hasValidConfiguration() {
        return isEmailValid() && isPasswordValid();
    }
    
    private boolean isEmailValid() {
        if (isBlank(adminEmail)) {
            logger.error("Admin email is not configured");
            return false;
        }
        
        if (!adminEmail.contains("@")) {
            logger.error("Admin email format is invalid: {}", adminEmail);
            return false;
        }
        
        return true;
    }
    
    private boolean isPasswordValid() {
        if (isBlank(adminPassword) || adminPassword.trim().length() < MIN_PASSWORD_LENGTH) {
            logger.error("Admin password must be at least {} characters long", MIN_PASSWORD_LENGTH);
            return false;
        }
        
        return true;
    }
    
    private void createAdminUserIfNeeded() {
        if (adminUserAlreadyExists()) {
            logger.info("Admin user already exists - skipping creation");
            return;
        }
        
        createNewAdminUser();
    }
    
    private boolean adminUserAlreadyExists() {
        boolean exists = utilisateurRepository.existsByEmail(adminEmail);
        if (exists) {
            logger.info("Found existing admin user: {}", adminEmail);
        }
        return exists;
    }
    
    private void createNewAdminUser() {
        logger.info("Creating new admin user: {}", adminEmail);
        
        Admin admin = buildAdminUser();
        utilisateurRepository.save(admin);
        
        logAdminCreationSuccess();
    }
    
    private Admin buildAdminUser() {
        return new Admin(
            cleanString(adminLastName),
            cleanString(adminFirstName),
            cleanString(adminEmail).toLowerCase(),
            passwordEncoder.encode(adminPassword),
            Role.ADMIN
        );
    }
    
    private void logAdminCreationSuccess() {
        logger.info("Admin user created successfully:");
        logger.info("  Email: {}", adminEmail);
        logger.info("  Name: {} {}", adminFirstName, adminLastName);
        logger.info("  Role: ADMIN");
        logger.info("  Password: [ENCRYPTED]");
    }
    
    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
    
    private String cleanString(String str) {
        return str != null ? str.trim() : "";
    }
}