package com.example.abroad.controller;

import com.example.abroad.model.User;
import com.example.abroad.service.FormatService;
import com.example.abroad.service.UserService;
import jakarta.servlet.http.HttpSession;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public record DashboardController(FormatService formatter, UserService userService) {
  @GetMapping("/")
  public String home(HttpSession session, Model model) {
    model.addAttribute("theme", userService.getTheme(session));
    User user = userService.getUser(session).orElse(null);
    if (user == null) {
      return "homepage";
    }
    model.addAttribute("formatter", formatter);
    if(user.isAdmin()) {
      return adminDashboard(model, user);
    }
    return showDashboard(model, user);
  }
  public String showDashboard(Model model, User user) {
    String displayName = user.displayName();
    String username = user.username();
    System.out.println(
      "Getting username from session: " + user.username());
    model.addAttribute("displayName", displayName);
    model.addAttribute("student", username); // Add this for navbar
    return "dashboard/student-dashboard :: page";
  }

  public String adminDashboard(Model model, User user) {

    model.addAttribute("displayName", user.displayName());

    return "dashboard/admin-dashboard :: page";  // Note the :: page suffix
  }

  @GetMapping("/hello")
  public String hello(Authentication authentication, Model model) {
    if (authentication != null) {
      System.out.println("User is authenticated as: " + authentication.getName());
      model.addAttribute("name", authentication.getName());
    } else {
      System.out.println("No authentication found");
    }
    return "hello";  // changed from "hello :: page"
  }

  @GetMapping("/test-auth")
  public String testAuth(Authentication authentication, Model model) {
    System.out.println("Test Auth endpoint called");
    if (authentication != null) {
      System.out.println("User is authenticated as: " + authentication.getName());
      model.addAttribute("username", authentication.getName());
      model.addAttribute("roles", authentication.getAuthorities());
    } else {
      System.out.println("No authentication found in /test-auth");
    }
    return "test-auth";
  }
}