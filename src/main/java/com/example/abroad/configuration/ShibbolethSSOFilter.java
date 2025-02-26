package com.example.abroad.configuration;

import java.io.IOException;

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

    public ShibbolethSSOFilter(UserService userService) {
        this.userService = userService;
    }

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

            // // Save user to session
            // userService.saveUserToSession(ssoUser, request.getSession());
            
            // // Create an authentication token and set it in the SecurityContext
            // UsernamePasswordAuthenticationToken auth =
            //         new UsernamePasswordAuthenticationToken(
            //                 ssoUser,
            //                 null,
            //                 List.of());
            // SecurityContextHolder.getContext().setAuthentication(auth);
        }
        filterChain.doFilter(request, response);
    }
}