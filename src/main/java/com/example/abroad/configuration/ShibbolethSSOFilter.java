package com.example.abroad.configuration;

import java.io.IOException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.abroad.model.User;
import com.example.abroad.service.UserService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class ShibbolethSSOFilter extends OncePerRequestFilter {

  private final UserService userService;

  public ShibbolethSSOFilter(UserService userService) {
    this.userService = userService;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request,
    HttpServletResponse response,
    FilterChain filterChain)
    throws ServletException, IOException {
    HttpSession session = request.getSession(false);
    if (session == null || userService.findUserFromSession(session).isEmpty()) {
      // Look for the SSO header – adjust header names as needed
      String ssoUsername = request.getHeader("REMOTE_USER");
      if (ssoUsername != null) {
        // Optionally, grab additional attributes
        String email = request.getHeader("Shib-Email");
        String displayName = request.getHeader("Shib-DisplayName");

        // Check if this user already exists in our DB
        Optional<? extends User> userOpt = userService.findByUsername(ssoUsername);
        User user;
        if (userOpt.isEmpty()) {
          // Create a new SSOUser (use defaults or additional header values as needed)
          user = new User.SSOUser(ssoUsername, email, User.Role.STUDENT, displayName, User.Theme.DEFAULT);
          userService.save(user);
        } else {
          user = userOpt.get();
        }
        request.getSession(true);
        userService.saveUserToSession(user, request.getSession());
      }
    }
    filterChain.doFilter(request, response);
  }
}