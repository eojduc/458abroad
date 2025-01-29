package com.example.abroad.Configurations;

import com.example.abroad.model.Admin;
import com.example.abroad.respository.AdminRepository;
import com.example.abroad.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class AdminInitializer implements CommandLineRunner {
    private final UserService userService;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private static final Logger logger = LoggerFactory.getLogger(AdminInitializer.class);

    @org.springframework.beans.factory.annotation.Autowired
    public AdminInitializer(

            UserService userService,
            AdminRepository adminRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userService = userService;
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Value("${app.initial-admin.username:superadmin}")
    private String initialAdminUsername;

    @Value("${app.initial-admin.email:admin@mycompany.com}")
    private String initialAdminEmail;

    @Value("${app.initial-admin.password}")
    private String initialAdminPassword;

    @Override
    public void run(String... args) {
        logger.info("Initializing admin user");
        try {
            if (!adminRepository.existsByUsername(initialAdminUsername)) {
                Admin firstAdmin = new Admin(
                        initialAdminUsername,
                        passwordEncoder.encode(initialAdminPassword),
                        initialAdminEmail,
                        "Administrator"
                );

                adminRepository.save(firstAdmin);

                logger.info("Initial admin created successfully");
            } else {
                logger.warn("Failed to create admin");
            }
        } catch (Exception e) {
            logger.error("Failed to create initial admin");
            throw new RuntimeException("Could not initialize admin user", e);
        }
    }
}