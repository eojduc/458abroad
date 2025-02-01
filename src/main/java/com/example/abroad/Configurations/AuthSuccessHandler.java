package com.example.abroad.Configurations;


import com.example.abroad.model.Role;
import com.example.abroad.respository.AdminRepository;
import com.example.abroad.respository.StudentRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class AuthSuccessHandler implements AuthenticationSuccessHandler {

  private final StudentRepository studentRepository;

  private final AdminRepository adminRepository;
  @Autowired
  public AuthSuccessHandler(StudentRepository studentRepository, AdminRepository adminRepository) {
    this.studentRepository = studentRepository;
    this.adminRepository = adminRepository;
  }

  //controls what is done after successfuly login
  @Override
  public void onAuthenticationSuccess(HttpServletRequest request,
    HttpServletResponse response,
    Authentication authentication) throws IOException {
    String userName = authentication.getName();
    String displayName = "";

    HttpSession session = request.getSession(true);

    // Invalidate any existing session
    if (!session.isNew()) {
      session.invalidate();
      session = request.getSession(true);
    }

    session.setAttribute("username", authentication.getName());
    Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
    session.setAttribute("role", authorities.stream()
      .findFirst()
      .map(GrantedAuthority::getAuthority)
      .map(role -> Role.valueOf(role)) // Convert string to Role enum
      .orElse(null));
    Role userRole = (Role) session.getAttribute("role");
    System.out.println("Role from session: " + userRole);

    switch (userRole) {
      case ROLE_ADMIN:
        displayName = adminRepository.findByUsername(userName).get().displayName();
        session.setAttribute("displayName", displayName);
        session.setAttribute("user", adminRepository.findByUsername(userName).get());
        System.out.println("DEBUG: Admin user set in session: " + userName);
        response.sendRedirect("/");
        break;
      case ROLE_STUDENT:
        displayName = studentRepository.findByUsername(userName).get().displayName();
        session.setAttribute("displayName", displayName);
        session.setAttribute("user", studentRepository.findByUsername(userName).get());
        System.out.println("DEBUG: Student user set in session: " + userName);
        response.sendRedirect("/");
        break;
      default:
        response.sendRedirect("/login");
        break;
    }
  }
}