package com.example.abroad.controller;

import com.example.abroad.model.Alerts;
import com.example.abroad.service.AccountService;
import com.example.abroad.service.AccountService.GetProfile;
import com.example.abroad.service.AccountService.UpdateProfile;
import com.example.abroad.service.AccountService.ChangePassword;
import com.example.abroad.service.FormatService;
import com.example.abroad.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.Optional;

@Controller
public record AccountController(
        AccountService accountService,
        FormatService formatter,
        UserService userService
) {
    @GetMapping("/profile")
    public String getProfile(HttpSession session, Model model,
                             @RequestParam Optional<String> error,
                             @RequestParam Optional<String> success,
                             @RequestParam Optional<String> warning,
                             @RequestParam Optional<String> info) {

        return switch (accountService.getProfile(session)) {
            case GetProfile.UserNotFound() -> "redirect:/login";
            case GetProfile.Success(var user) -> {
                model.addAttribute("user", user);
                model.addAttribute("alerts", new Alerts(error, success, warning, info));
                model.addAttribute("formatter", formatter);
                model.addAttribute("theme", userService.getTheme(session));
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
                yield "redirect:/profile?success=Profile updated successfully";
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
            case ChangePassword.IncorrectPassword() -> "redirect:/profile?error=Current password is incorrect";
            case ChangePassword.PasswordMismatch() -> "redirect:/profile?error=New passwords do not match";
            case ChangePassword.Success(var updatedUser) -> {
                session.setAttribute("user", updatedUser);
                yield "redirect:/profile?success=Password updated successfully";
            }
        };
    }


    @PostMapping("/profile/theme")
    public String setTheme(@RequestParam String theme, HttpSession session) {
        userService.setTheme(theme, session);
        return "redirect:/profile";
    }
}