package com.example.abroad.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.abroad.model.User;
import com.example.abroad.service.AuditService;
import com.example.abroad.service.UserService;
import com.example.abroad.view.Alerts;
import com.warrenstrange.googleauth.GoogleAuthenticator;

import org.springframework.ui.Model;

import java.util.Optional;

import org.slf4j.MDC;

import jakarta.servlet.http.HttpSession;

@Controller
public record MfaController(UserService userService, AuditService auditService) {
    @GetMapping("/mfa")
    public String showMfaForm(
            HttpSession session, 
            Model model,
            @RequestParam Optional<String> error,
            @RequestParam Optional<String> success,
            @RequestParam Optional<String> warning,
            @RequestParam Optional<String> info
    ) {       
        User pendingUser = (User) session.getAttribute("mfaPendingUser");
        if (pendingUser == null) {
            return "redirect:/login?error=MFA session expired.";
        }
        model.addAttribute("alerts", new Alerts(error, success, warning, info));
        return "auth/mfa";
    }

    @PostMapping("/mfa")
    public String processMfa(@RequestParam String code, HttpSession session) {
        User pendingUser = (User) session.getAttribute("mfaPendingUser");
        if (pendingUser == null || !(pendingUser instanceof User.LocalUser localUser)) {
            return "redirect:/login?error=MFA session expired.";
        }
        
        // Verify TOTP
        GoogleAuthenticator gAuth = new GoogleAuthenticator();
        try {
            int totpCode = Integer.parseInt(code);
            boolean isValid = gAuth.authorize(localUser.mfaSecret(), totpCode);
            if (!isValid) {
                return "redirect:/mfa?error=Invalid MFA code";
            }
        } catch (NumberFormatException e) {
            return "redirect:/mfa?error=Invalid MFA code format";
        }
        
        // TOTP verified, so complete the login:
        userService.saveUserToSession(localUser, session);
        session.removeAttribute("mfaPendingUser");
        MDC.put("username", localUser.username());
        auditService.logEvent("Local User " + localUser.username() + " completed MFA and logged in.");
        
        return "redirect:/";
    }
}
