package com.example.abroad.controller;

import com.example.abroad.model.User;
import jakarta.servlet.http.HttpSession;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class DashboardController {
  @GetMapping("/")
  public String home(HttpSession session, Model model) {
    User user = (User) session.getAttribute("user");
    String username = user.username();
    if (username == null) {
      return "homepage";
    }
    if(user.isAdmin()) {
      return adminDashboard(session, model);
    }
    return showDashboard(session, model);
  }
  public String showDashboard(HttpSession session, Model model) {
    User user = (User) session.getAttribute("user");
    String displayName = user.displayName();
    String username = user.username();
    System.out.println(
      "Getting username from session: " + user.username());
    model.addAttribute("displayName", displayName);
    model.addAttribute("student", username); // Add this for navbar
    return "dashboard/student-dashboard :: page";
  }

  public String adminDashboard(HttpSession session, Model model) {
    User user = (User) session.getAttribute("user");
    var displayName = Optional.ofNullable(user.displayName())
      .filter(obj -> obj instanceof String)
      .map(obj -> (String) obj)
      .orElse("Admin"); // Default fallback

    model.addAttribute("displayName", displayName);

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