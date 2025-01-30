package com.example.abroad.Configurations;

import com.example.abroad.model.Admin;
import com.example.abroad.respository.AdminRepository;
import com.example.abroad.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements CommandLineRunner {

  private final UserService userService;
  private final AdminRepository adminRepository;
  private final PasswordEncoder passwordEncoder;
  @Value("${app.initial-admin.username:superadmin}")
  private String initialAdminUsername;
  @Value("${app.initial-admin.email:admin@mycompany.com}")
  private String initialAdminEmail;
  @Value("${app.initial-admin.password}")
  private String initialAdminPassword;

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

  @Override
  public void run(String... args) {
    System.out.println("Initializing admin user");
    try {
      if (!adminRepository.existsByUsername(initialAdminUsername)) {
        Admin firstAdmin = new Admin(
          initialAdminUsername,
          passwordEncoder.encode(initialAdminPassword),
          initialAdminEmail,
          "Super Admin"
        );

        adminRepository.save(firstAdmin);

        System.out.println("Initial admin created successfully");
      }
    } catch (Exception e) {
      System.out.println("Failed to create initial admin");
      throw new RuntimeException("Could not initialize admin user", e);
    }
  }
}