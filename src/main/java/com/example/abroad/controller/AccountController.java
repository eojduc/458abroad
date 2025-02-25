package com.example.abroad.controller;

import com.example.abroad.model.Alerts;
import com.example.abroad.model.User.Theme;
import com.example.abroad.service.page.AccountService;
import com.example.abroad.service.page.AccountService.ChangePassword.IncorrectPassword;
import com.example.abroad.service.page.AccountService.ChangePassword.NotLocalUser;
import com.example.abroad.service.page.AccountService.ChangePassword.PasswordMismatch;
import com.example.abroad.service.page.AccountService.GetProfile.Success;
import com.example.abroad.service.page.AccountService.GetProfile.UserNotFound;
import com.example.abroad.service.page.AccountService.UpdateProfile;
import com.example.abroad.service.page.AccountService.ChangePassword;
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
            case UserNotFound() -> "redirect:/login";
            case Success(var user) -> {
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
            case IncorrectPassword() -> "redirect:/profile?error=Current password is incorrect";
            case PasswordMismatch() -> "redirect:/profile?error=New passwords do not match";
            case ChangePassword.Success(var updatedUser) -> {
                session.setAttribute("user", updatedUser);
                yield "redirect:/profile?success=Password updated successfully";
            }
          case NotLocalUser() -> "redirect:/profile?error=Cannot change password for SSO user";
        };
    }


    @PostMapping("/profile/theme")
    public String setTheme(@RequestParam Theme theme, HttpSession session) {
        userService.setTheme(theme, session);
        return "redirect:/profile";
    }
}