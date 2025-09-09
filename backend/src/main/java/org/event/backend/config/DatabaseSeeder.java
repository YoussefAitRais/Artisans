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
 * Database Seeder for Admin Users
 * 
 * Clean Code Example for Beginners:
 * 
 * This class demonstrates:
 * - Automatic data seeding on application startup
 * - Configuration-driven admin creation
 * - Safe password handling with encryption
 * - Proper error handling and logging
 * - Check-before-create pattern to avoid duplicates
 * 
 * How it works:
 * 1. Runs automatically when Spring Boot starts up
 * 2. Checks if admin user already exists
 * 3. Creates admin user if not found
 * 4. Uses encrypted passwords for security
 * 5. Logs the process for monitoring
 */
@Component
public class DatabaseSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseSeeder.class);

    // === DEPENDENCIES ===
    
    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    
    // === CONFIGURATION VALUES ===
    
    /**
     * Admin email from application properties
     * Default: admin@artisan.com if not specified
     */
    @Value("${app.admin.email:admin@artisan.com}")
    private String adminEmail;
    
    /**
     * Admin password from application properties
     * Default: admin123 if not specified
     */
    @Value("${app.admin.password:admin123}")
    private String adminPassword;
    
    /**
     * Admin first name from application properties
     * Default: Admin if not specified
     */
    @Value("${app.admin.firstname:Admin}")
    private String adminFirstName;
    
    /**
     * Admin last name from application properties
     * Default: System if not specified
     */
    @Value("${app.admin.lastname:System}")
    private String adminLastName;
    
    /**
     * Whether admin seeding is enabled
     * Default: true if not specified
     */
    @Value("${app.admin.seed.enabled:true}")
    private boolean seedingEnabled;

    // === CONSTRUCTOR ===
    
    /**
     * Constructor with dependency injection
     * 
     * @param utilisateurRepository - Repository for user operations
     * @param passwordEncoder - Service for password encryption
     */
    public DatabaseSeeder(UtilisateurRepository utilisateurRepository, 
                         PasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // === MAIN SEEDING METHOD ===
    
    /**
     * Main method that runs on application startup
     * 
     * Clean Code Approach:
     * 1. Check if seeding is enabled
     * 2. Validate configuration
     * 3. Check if admin already exists
     * 4. Create admin if needed
     * 5. Log results for monitoring
     */
    @Override
    public void run(String... args) throws Exception {
        logger.info("🌱 Starting Database Seeding Process...");
        
        // Step 1: Check if seeding is enabled
        if (!seedingEnabled) {
            logger.info("⏭️ Admin seeding is disabled in configuration");
            return;
        }
        
        // Step 2: Validate configuration
        if (!isValidConfiguration()) {
            logger.error("❌ Invalid admin configuration - skipping seeding");
            return;
        }
        
        try {
            // Step 3: Check and create admin
            createAdminIfNotExists();
            
            logger.info("✅ Database seeding completed successfully");
            
        } catch (Exception e) {
            logger.error("❌ Error during database seeding: {}", e.getMessage(), e);
            // Don't re-throw - let application continue even if seeding fails
        }
    }

    // === HELPER METHODS ===
    
    /**
     * Validates the admin configuration from properties
     * 
     * @return true if configuration is valid, false otherwise
     */
    private boolean isValidConfiguration() {
        if (adminEmail == null || adminEmail.trim().isEmpty()) {
            logger.error("Admin email is not configured");
            return false;
        }
        
        if (adminPassword == null || adminPassword.trim().length() < 6) {
            logger.error("Admin password is not configured or too short (minimum 6 characters)");
            return false;
        }
        
        if (!adminEmail.contains("@")) {
            logger.error("Admin email format is invalid: {}", adminEmail);
            return false;
        }
        
        return true;
    }
    
    /**
     * Creates admin user if one doesn't already exist
     * 
     * Clean Code Principle: One method, one responsibility
     */
    private void createAdminIfNotExists() {
        logger.info("🔍 Checking if admin user exists with email: {}", adminEmail);
        
        // Check if admin already exists
        if (utilisateurRepository.existsByEmail(adminEmail)) {
            logger.info("👤 Admin user already exists - skipping creation");
            return;
        }
        
        // Create new admin user
        logger.info("👨‍💼 Creating new admin user...");
        
        Admin admin = new Admin(
            adminLastName.trim(),
            adminFirstName.trim(),
            adminEmail.trim().toLowerCase(),
            passwordEncoder.encode(adminPassword),
            Role.ADMIN
        );
        
        // Save to database
        utilisateurRepository.save(admin);
        
        logger.info("✅ Admin user created successfully:");
        logger.info("   📧 Email: {}", adminEmail);
        logger.info("   👤 Name: {} {}", adminFirstName, adminLastName);
        logger.info("   🔐 Password: [ENCRYPTED]");
        logger.info("   🎭 Role: ADMIN");
    }
}