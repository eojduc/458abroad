package com.example.abroad.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SessionController {

    @PostMapping("/keep-alive")
    public ResponseEntity<Void> keepAlive(HttpSession session) {
        // This endpoint just resets the session inactivity timer
        return ResponseEntity.ok().build();
    }
}