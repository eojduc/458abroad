package com.example.abroad.controller;

import com.example.abroad.view.Alerts;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.example.abroad.model.User;
import com.example.abroad.model.User.Theme;
import com.example.abroad.service.page.AccountService;
import com.example.abroad.service.page.AccountService.ChangePassword.IncorrectPassword;
import com.example.abroad.service.page.AccountService.ChangePassword.NotLocalUser;
import com.example.abroad.service.page.AccountService.ChangePassword.PasswordMismatch;
import com.example.abroad.service.page.AccountService.GetProfile.Success;
import com.example.abroad.service.page.AccountService.GetProfile.UserNotFound;
import com.example.abroad.service.page.AccountService.UpdateProfile;
import com.example.abroad.service.page.AccountService.ChangePassword;
import com.example.abroad.service.AuditService;
import com.example.abroad.service.FormatService;
import com.example.abroad.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Controller
public record AccountController(
        AccountService accountService,
        FormatService formatter,
        UserService userService,
        AuditService auditService
) {
    @GetMapping("/profile")
    public String getProfile(HttpSession session, Model model,
                             @RequestParam Optional<String> error,
                             @RequestParam Optional<String> success,
                             @RequestParam Optional<String> warning,
                             @RequestParam Optional<String> info) {

        return switch (accountService.getProfile(session)) {
            case UserNotFound() -> "redirect:/login";
            case Success(var user) -> {
                model.addAttribute("user", user);
                model.addAttribute("isStudent", userService.isStudent(user));
                model.addAttribute("isAdmin", userService.isAdmin(user));
                model.addAttribute("alerts", new Alerts(error, success, warning, info));
                model.addAttribute("formatter", formatter);
                model.addAttribute("isLocalUser", user instanceof User.LocalUser);
                yield "profile :: page";
            }
        };
    }

    @PostMapping("/profile/update")
    public String updateProfile(
            @RequestParam String displayName,
            @RequestParam String email,
            HttpSession session,
            Model model) {

        model.addAttribute("formatter", formatter);

        return switch (accountService.updateProfile(displayName, email, session)) {
            case UpdateProfile.UserNotFound() -> "redirect:/login";
            case UpdateProfile.Success(var updatedUser) -> {
                model.addAttribute("user", updatedUser);
                session.setAttribute("user", updatedUser);
                yield "redirect:/profile?success=Profile updated successfully#profile-settings";
            }
        };
    }

    @PostMapping("/profile/change-password")
    public String changePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            HttpSession session) {

        return switch (accountService.changePassword(currentPassword, newPassword, confirmPassword, session)) {
            case ChangePassword.UserNotFound() -> "redirect:/login";
            case IncorrectPassword() -> "redirect:/profile?error=Current password is incorrect#change-password";
            case PasswordMismatch() -> "redirect:/profile?error=New passwords do not match#change-password";
            case ChangePassword.Success(var updatedUser) -> {
                session.setAttribute("user", updatedUser);
                yield "redirect:/profile?success=Password updated successfully#change-password";
            }
          case NotLocalUser() -> "redirect:/profile?error=Cannot change password for SSO user#change-password";
        };
    }


    @PostMapping("/profile/theme")
    public String setTheme(@RequestParam Theme theme, HttpSession session) {
        userService.setTheme(theme, session);
        return "redirect:/profile#set-theme";
    }


    @PostMapping("/profile/set-ulink")
    public String setULink(@RequestParam String uLink, @RequestParam String pin, HttpSession session) {
        return switch (accountService.setULink(uLink, pin, session)) {
            case AccountService.SetULink.Success() -> "redirect:/profile?success=ULink set successfully#ulink";
            case AccountService.SetULink.UserNotFound() -> "redirect:/login";
            case AccountService.SetULink.IncorrectPin() -> "redirect:/profile?error=Invalid PIN#ulink";
            case AccountService.SetULink.ConnectionError() -> "redirect:/profile?error=Error Connecting to ULink Server#ulink";
            case AccountService.SetULink.UserNotLocalUser() -> "redirect:/profile?error=Cannot set ULink for SSO user#ulink";
            case AccountService.SetULink.UsernameInUse() -> "redirect:/profile?error=Username already in use#ulink";
            case AccountService.SetULink.AlreadySet() -> "redirect:/profile?info=ULink already set#ulink";
        };
    }

    @GetMapping("/profile/mfa/enroll")
    public String showMfaEnrollment(
            HttpSession session, 
            Model model, 
            @RequestParam Optional<String> error,
            @RequestParam Optional<String> success,
            @RequestParam Optional<String> warning,
            @RequestParam Optional<String> info
    ) {
        var optUser = userService.findUserFromSession(session);
        if (optUser.isEmpty() || !(optUser.get() instanceof User.LocalUser localUser)) {
            return "redirect:/login?error=Please log in to enroll in MFA.";
        }
        
        if (localUser.isMfaEnabled()) {
            return "redirect:/profile?info=MFA is already enabled.";
        }

        GoogleAuthenticator gAuth = new GoogleAuthenticator();
        GoogleAuthenticatorKey key = gAuth.createCredentials();
        String secret = key.getKey();
        
        session.setAttribute("mfaEnrollmentSecret", secret);
        
        // Prepare an otpauth URL
        String issuer = "458Abroad";
        String label = issuer + ":" + localUser.username();
        String encodedLabel = URLEncoder.encode(label, StandardCharsets.UTF_8);
        String encodedIssuer = URLEncoder.encode(issuer, StandardCharsets.UTF_8);
        String otpauthUrl = "otpauth://totp/" + encodedLabel
                + "?secret=" + secret
                + "&issuer=" + encodedIssuer;
        
        model.addAttribute("user", localUser);
        model.addAttribute("alerts", new Alerts(error, success, warning, info));
        model.addAttribute("isStudent", userService.isStudent(localUser));
        model.addAttribute("isAdmin", userService.isAdmin(localUser));
        model.addAttribute("formatter", formatter);
        model.addAttribute("isLocalUser", localUser instanceof User.LocalUser);
        model.addAttribute("qrUrl", otpauthUrl);
        model.addAttribute("secret", secret);
        return "auth/mfa-enroll";
    }

    @PostMapping("/profile/mfa/verify")
    public String verifyMfaEnrollment(@RequestParam String code, HttpSession session) {
        var optUser = userService.findUserFromSession(session);
        if (optUser.isEmpty() || !(optUser.get() instanceof User.LocalUser localUser)) {
            return "redirect:/login?error=Please log in to enroll in MFA.";
        }
        
        String secret = (String) session.getAttribute("mfaEnrollmentSecret");
        if (secret == null) {
            return "redirect:/profile?error=MFA enrollment session expired. Please try again.";
        }
        
        GoogleAuthenticator gAuth = new GoogleAuthenticator();
        try {
            int totpCode = Integer.parseInt(code);
            boolean isValid = gAuth.authorize(secret, totpCode);
            if (!isValid) {
                return "redirect:/profile/mfa/enroll?error=Invalid code. Please try again.";
            }
        } catch (NumberFormatException e) {
            return "redirect:/profile/mfa/enroll?error=Invalid code format. Please try again.";
        }
        
        userService.updateMfaSettings(localUser.username(), true, secret);
        var updatedUser = userService.findByUsername(localUser.username()).orElse(null);
        
        session.removeAttribute("mfaEnrollmentSecret");
        session.setAttribute("user", updatedUser);
        
        auditService.logEvent("User " + localUser.username() + " successfully enrolled in MFA.");
        return "redirect:/profile?success=MFA enrollment successful";
    }

    @PostMapping("/profile/mfa/disable")
    public String disableMfa(HttpSession session) {
        var optUser = userService.findUserFromSession(session);
        if (optUser.isEmpty() || !(optUser.get() instanceof User.LocalUser localUser)) {
            return "redirect:/login?error=Please log in.";
        }
        userService.updateMfaSettings(localUser.username(), false, null);
        var updatedUser = userService.findByUsername(localUser.username()).orElse(null);
        session.setAttribute("user", updatedUser);
        auditService.logEvent("User " + localUser.username() + " disabled MFA.");
        return "redirect:/profile?success=MFA disabled";
    }
}