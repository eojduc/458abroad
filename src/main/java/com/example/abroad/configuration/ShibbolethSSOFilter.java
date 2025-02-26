package com.example.abroad.configuration;

import com.example.abroad.model.User;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

import java.util.Optional;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.abroad.controller.admin.EditProgramPageController;
import com.example.abroad.service.UserService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;

@Component
public class ShibbolethSSOFilter extends OncePerRequestFilter {

    private final UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(ShibbolethSSOFilter.class);


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
                                    throws ServletException, IOException {
        logger.debug("Entered Filter");

        // Check if Shibboleth provided authentication headers exist.
        String remoteUser = request.getHeader("REMOTE_USER");
        if (remoteUser != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Extract additional attributes
            String email = request.getHeader("Shib-Email");
            String displayName = request.getHeader("Shib-DisplayName");

            logger.info("Email Info received: " + email);
            logger.info("Display Name Info received: " + displayName);
            
            // // Create SSO User
            // SSOUser ssoUser = new SSOUser(remoteUser, email, User.Role.STUDENT, displayName, User.Theme.DEFAULT);

        // Check if this user already exists in our DB
        var ssoUsername = "";
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

    filterChain.doFilter(request, response);
  }
}