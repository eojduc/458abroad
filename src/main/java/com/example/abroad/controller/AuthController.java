package com.example.abroad.controller;

import com.example.abroad.view.Alerts;
import com.example.abroad.service.page.AuthService;
import com.example.abroad.service.page.AuthService.CheckLoginStatus;
import com.example.abroad.service.page.AuthService.Login;
import com.example.abroad.service.page.AuthService.Logout;
import com.example.abroad.service.page.AuthService.RegisterResult;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@Controller
public record AuthController(AuthService authService) {

  public static Logger logger = LoggerFactory.getLogger(AuthController.class);

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

  @PostMapping("/login")
  public String login(
      @RequestParam String username,
      @RequestParam String password,
      HttpSession session) {
    return switch (authService.login(username, password, session)) {
      case Login.Success(var user) -> "redirect:/";
      case Login.InvalidCredentials() -> "redirect:/login?error=Invalid username or password";
    };
  }

  @GetMapping("/logout")
  public String logout(HttpSession session) {
    return switch (authService.logout(session)) {
      case Logout.LocalUserSuccess() -> "redirect:/login?info=You have been logged out";
      case Logout.SSOUserSuccess(String redirectUrl) -> "redirect:/Shibboleth.sso/Logout?return=" + redirectUrl;
      case Logout.NotLoggedIn() -> "redirect:/";
    };
  }

  @GetMapping("/register")
  public String showRegistrationForm(
      HttpSession session,
      @RequestParam Optional<String> error,
      @RequestParam Optional<String> info,
      @RequestParam Optional<String> success,
      @RequestParam Optional<String> warning,
      Model model) {
    return switch (authService.checkLoginStatus(session)) {
      case CheckLoginStatus.AlreadyLoggedIn() -> "redirect:/";
      case CheckLoginStatus.NotLoggedIn() -> {
        model.addAttribute("alerts", new Alerts(error, success, warning, info));
        yield "auth/register";
      }
    };
  }

  @PostMapping("/register")
  public String registerUser(
      @RequestParam String username,
      @RequestParam String displayName,
      @RequestParam String email,
      @RequestParam String password,
      @RequestParam String confirmPassword,
      HttpSession session,
      Model model) {

    return switch (authService.registerUser(username, displayName, email, password, confirmPassword, session)) {
      case RegisterResult.Success() -> "redirect:/";
      case RegisterResult.UsernameExists() -> "redirect:/register?error=Username is already taken";
      case RegisterResult.EmailExists() -> "redirect:/register?error=Email is already registered";
      case RegisterResult.PasswordMismatch() -> "redirect:/register?error=New passwords do not match";
      case RegisterResult.AuthenticationError(var error) -> "redirect:/register?error=Registration failed. Please try again.";
    };
  }
}