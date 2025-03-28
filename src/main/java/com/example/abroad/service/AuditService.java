package com.example.abroad.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AuditService {
    private static final Logger auditLogger = LoggerFactory.getLogger("auditLogger");

    public void logEvent(String message) {
        auditLogger.info(message);
    }
}