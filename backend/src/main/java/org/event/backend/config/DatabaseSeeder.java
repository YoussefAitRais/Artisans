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

/**
 * Database initialization service for seeding administrative users.
 * 
 * This component automatically creates default admin users during application startup
 * following Spring Boot's CommandLineRunner pattern. It ensures idempotent operations
 * by checking for existing data before creation.
 * 
 * @author Artisan Platform Team
 * @version 1.0
 */
@Component
public class DatabaseSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseSeeder.class);
    private static final int MIN_PASSWORD_LENGTH = 6;
    
    // ============ DEPENDENCIES ============
    
    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    
    // ============ CONFIGURATION PROPERTIES ============
    
    /**
     * Admin email address for the default administrator account.
     * Configurable via application properties: app.admin.email
     */
    @Value("${app.admin.email:admin@artisan.com}")
    private String adminEmail;
    
    /**
     * Admin password for the default administrator account.
     * Configurable via application properties: app.admin.password
     */
    @Value("${app.admin.password:admin123}")
    private String adminPassword;
    
    /**
     * Admin first name for the default administrator account.
     * Configurable via application properties: app.admin.firstname
     */
    @Value("${app.admin.firstname:Admin}")
    private String adminFirstName;
    
    /**
     * Admin last name for the default administrator account.
     * Configurable via application properties: app.admin.lastname
     */
    @Value("${app.admin.lastname:System}")
    private String adminLastName;
    
    /**
     * Flag to enable or disable admin seeding.
     * Configurable via application properties: app.admin.seed.enabled
     */
    @Value("${app.admin.seed.enabled:true}")
    private boolean seedingEnabled;

    // ============ CONSTRUCTOR ============
    
    /**
     * Constructor for dependency injection.
     * 
     * @param utilisateurRepository repository for user data access
     * @param passwordEncoder service for secure password hashing
     */
    public DatabaseSeeder(UtilisateurRepository utilisateurRepository, 
                         PasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ============ MAIN EXECUTION METHOD ============
    
    /**
     * Executes the database seeding process during application startup.
     * 
     * This method follows a simple, linear flow:
     * 1. Check if seeding is enabled
     * 2. Validate configuration
     * 3. Create admin user if needed
     * 
     * @param args command line arguments (unused)
     */
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
    
    /**
     * Checks if database seeding is enabled in configuration.
     * 
     * @return true if seeding is enabled, false otherwise
     */
    private boolean isSeedingEnabled() {
        if (!seedingEnabled) {
            logger.info("Admin seeding is disabled in configuration");
            return false;
        }
        return true;
    }
    
    /**
     * Validates all required configuration properties.
     * 
     * @return true if configuration is valid, false otherwise
     */
    private boolean hasValidConfiguration() {
        return isEmailValid() && isPasswordValid();
    }
    
    /**
     * Validates the admin email configuration.
     * 
     * @return true if email is valid, false otherwise
     */
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
    
    /**
     * Validates the admin password configuration.
     * 
     * @return true if password is valid, false otherwise
     */
    private boolean isPasswordValid() {
        if (isBlank(adminPassword) || adminPassword.trim().length() < MIN_PASSWORD_LENGTH) {
            logger.error("Admin password must be at least {} characters long", MIN_PASSWORD_LENGTH);
            return false;
        }
        
        return true;
    }
    
    /**
     * Creates an admin user if one doesn't already exist.
     */
    private void createAdminUserIfNeeded() {
        if (adminUserAlreadyExists()) {
            logger.info("Admin user already exists - skipping creation");
            return;
        }
        
        createNewAdminUser();
    }
    
    /**
     * Checks if an admin user already exists with the configured email.
     * 
     * @return true if admin exists, false otherwise
     */
    private boolean adminUserAlreadyExists() {
        boolean exists = utilisateurRepository.existsByEmail(adminEmail);
        if (exists) {
            logger.info("Found existing admin user: {}", adminEmail);
        }
        return exists;
    }
    
    /**
     * Creates and saves a new admin user to the database.
     */
    private void createNewAdminUser() {
        logger.info("Creating new admin user: {}", adminEmail);
        
        Admin admin = buildAdminUser();
        utilisateurRepository.save(admin);
        
        logAdminCreationSuccess();
    }
    
    /**
     * Builds a new Admin entity with the configured properties.
     * 
     * @return configured Admin entity
     */
    private Admin buildAdminUser() {
        return new Admin(
            cleanString(adminLastName),
            cleanString(adminFirstName),
            cleanString(adminEmail).toLowerCase(),
            passwordEncoder.encode(adminPassword),
            Role.ADMIN
        );
    }
    
    /**
     * Logs successful admin user creation with details.
     */
    private void logAdminCreationSuccess() {
        logger.info("Admin user created successfully:");
        logger.info("  Email: {}", adminEmail);
        logger.info("  Name: {} {}", adminFirstName, adminLastName);
        logger.info("  Role: ADMIN");
        logger.info("  Password: [ENCRYPTED]");
    }
    
    /**
     * Checks if a string is null, empty, or contains only whitespace.
     * 
     * @param str the string to check
     * @return true if the string is blank, false otherwise
     */
    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
    
    /**
     * Cleans a string by trimming whitespace and handling null values.
     * 
     * @param str the string to clean
     * @return cleaned string or empty string if input was null
     */
    private String cleanString(String str) {
        return str != null ? str.trim() : "";
    }
}