package com.example.abroad.Configurations;


import com.example.abroad.model.Role;
import com.example.abroad.respository.AdminRepository;
import com.example.abroad.respository.StudentRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class AuthSuccessHandler implements AuthenticationSuccessHandler {
  @Autowired
  private static StudentRepository studentRepository;

  @Autowired
  private static AdminRepository adminRepository;

  //controls what is done after successfuly login
  @Override
  public void onAuthenticationSuccess(HttpServletRequest request,
    HttpServletResponse response,
    Authentication authentication) throws IOException {
    String userName = authentication.getName();
    String displayName = "";
    request.getSession().setAttribute("username", authentication.getName());
    System.out.println(
      "userName in onAuthenticationSuccess is " + request.getSession().getAttribute("username"));
    Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
    request.getSession().setAttribute("role", authorities.stream()
      .findFirst()
      .map(GrantedAuthority::getAuthority)
      .map(role -> Role.valueOf(role)) // Convert string to Role enum
      .orElse(null));
    Role userRole = (Role) request.getSession().getAttribute("role");
    System.out.println("Role from session: " + userRole);

    switch (userRole) {
      case ROLE_ADMIN:
        displayName = adminRepository.findByUsername(userName).get().displayName();
        request.getSession().setAttribute("displayName", displayName);
        response.sendRedirect("/admin/dashboard");
        break;
      case ROLE_STUDENT:
        displayName = studentRepository.findByUsername(userName).get().displayName();
        request.getSession().setAttribute("displayName", displayName);
        response.sendRedirect("/dashboard");
        break;
      default:
        response.sendRedirect("/login");
        break;
    }
  }
}