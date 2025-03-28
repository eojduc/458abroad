package com.example.abroad.configuration;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import com.example.abroad.model.User;

import java.io.IOException;

@Component
public class MDCFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // HttpServletRequest httpRequest = (HttpServletRequest) request;
        // String ip = httpRequest.getRemoteAddr();
        // MDC.put("ip", ip);

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String ip = httpRequest.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty()) {
            ip = ip.split(",")[0].trim();
        } else {
            ip = httpRequest.getRemoteAddr();
        }
        MDC.put("ip", ip);

        String username = "ANONYMOUS";
        HttpSession session = httpRequest.getSession(false);
        if (session != null) {
            Object userObj = session.getAttribute("user");
            if (userObj != null && userObj instanceof User) {
                username = ((User) userObj).username();
            }
        }
        MDC.put("username", username);

        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove("ip");
            MDC.remove("username");
        }
    }
}