package com.example.abroad.controller;

import com.example.abroad.model.Alerts;
import com.example.abroad.service.AuthService;
import com.example.abroad.service.AuthService.CheckLoginStatus;
import com.example.abroad.service.AuthService.RegisterResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public record AuthController(AuthService authService) {

  @GetMapping("/login")
  public String showLoginForm(
          HttpSession session,
          @RequestParam Optional<String> error,
          @RequestParam Optional<String> info,
          @RequestParam Optional<String> success,
          @RequestParam Optional<String> warning,
          Model model) {

    return switch (authService.checkLoginStatus(session)) {
      case CheckLoginStatus.AlreadyLoggedIn() ->
              "redirect:/?info=You are already logged in";
      case CheckLoginStatus.NotLoggedIn() -> {
        model.addAttribute("alerts", new Alerts(error, success, warning, info));
        yield "auth/login";
      }
    };
  }

  @GetMapping("/logout")
  public String logout(HttpSession session) {
    session.invalidate();
    return "redirect:/login?info=You have been logged out";
  }

  @GetMapping("/register")
  public String showRegistrationForm(HttpSession session) {
    return switch (authService.checkLoginStatus(session)) {
      case CheckLoginStatus.AlreadyLoggedIn() -> "redirect:/";
      case CheckLoginStatus.NotLoggedIn() -> "auth/register";
    };
  }

  @PostMapping("/register")
  public String registerUser(
          @RequestParam String username,
          @RequestParam String displayName,
          @RequestParam String email,
          @RequestParam String password,
          HttpServletRequest request,
          HttpServletResponse response,
          Model model) {

    return switch (authService.registerUser(username, displayName, email, password, request)) {
      case RegisterResult.Success(var authentication) -> {
        try {
          authService.handleSuccessfulAuthentication(request, response, authentication);
          yield null; // Redirect is handled by authentication success handler
        } catch (Exception e) {
          yield "redirect:/login";
        }
      }
      case RegisterResult.UsernameExists() -> {
        model.addAttribute("error", "Username is already taken");
        yield "auth/register";
      }
      case RegisterResult.EmailExists() -> {
        model.addAttribute("error", "Email is already registered");
        yield "auth/register";
      }
      case RegisterResult.AuthenticationError(var error) -> {
        model.addAttribute("error", "Registration failed. Please try again.");
        yield "auth/register";
      }
    };
  }
}