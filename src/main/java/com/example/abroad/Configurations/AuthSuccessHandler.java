package com.example.abroad.Configurations;


import com.example.abroad.model.Role;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;

@Component
public class AuthSuccessHandler implements AuthenticationSuccessHandler {
    //controls what is done after successfuly login
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        request.getSession().setAttribute("username", authentication.getName());
        System.out.println("userName in onAuthenticationSuccess is " + request.getSession().getAttribute("username"));
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        request.getSession().setAttribute("role", authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .map(role -> Role.valueOf(role)) // Convert string to Role enum
                .orElse(null));
        Role userRole = (Role) request.getSession().getAttribute("role");
        System.out.println("Role from session: " + userRole);

        switch (userRole) {
            case ROLE_ADMIN:
                response.sendRedirect("/admin/dashboard");
                break;
            case ROLE_STUDENT:
                response.sendRedirect("/dashboard");
                break;
            default:
                response.sendRedirect("/login");
                break;
        }
    }
}