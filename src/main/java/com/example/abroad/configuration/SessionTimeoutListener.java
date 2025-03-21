package com.example.abroad.configuration;

import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SessionTimeoutListener implements HttpSessionListener {

    private final int TIMEOUT = 2;

    private static final Logger logger = LoggerFactory.getLogger(SessionTimeoutListener.class);

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        // Set session timeout to 15 minutes (in seconds)
        se.getSession().setMaxInactiveInterval(30); //changed TIMEOUT * 60 to just 30
        // Store the creation time for 24-hour expiry check
        se.getSession().setAttribute("sessionCreationTime", System.currentTimeMillis());
        logger.info("Session created with ID: {}", se.getSession().getId());
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        logger.info("Session destroyed with ID: {}", se.getSession().getId());
    }
}