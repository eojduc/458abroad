package com.example.abroad.configuration;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;

public class SessionExpiryInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(SessionExpiryInterceptor.class);
    private static final long MAX_SESSION_DURATION = 24 * 60 * 60 * 1000; // 24 hours in milliseconds

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);

        if (session != null) {
            Long creationTime = (Long) session.getAttribute("sessionCreationTime");

            if (creationTime != null) {
                long currentTime = System.currentTimeMillis();

                // Check if session has been active for more than 24 hours
                if (currentTime - creationTime > MAX_SESSION_DURATION) {
                    logger.info("Session exceeded 24-hour limit: {}", session.getId());
                    session.invalidate();
                    response.sendRedirect("/login?info=Your session has expired due to maximum time limit. Please log in again.");
                    return false;
                }
            }
        }

        return true;
    }
}