package com.example.abroad.controller;

import com.example.abroad.model.User;
import com.example.abroad.service.UserService;
import jakarta.servlet.http.HttpSession;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public record DashboardController(UserService userService) {
  @GetMapping("/")
  public String home(HttpSession session, Model model) {
    var user = userService.getUser(session).orElse(null);
    if (user == null) {
      return "homepage";
    }
    if(user.isAdmin()) {
      return adminDashboard(model, user);
    }
    return showDashboard(model, user);
  }
  public String showDashboard(Model model, User user) {
    System.out.println(
      "Getting username from session: " + user.username());
    model.addAttribute("displayName", user.displayName());
    model.addAttribute("student", user.username()); // Add this for navbar
    return "dashboard/student-dashboard :: page";
  }

  public String adminDashboard(Model model, User user) {

    model.addAttribute("displayName", user.displayName());

    return "dashboard/admin-dashboard :: page";  // Note the :: page suffix
  }

  @GetMapping("/welcome")
  public String welcome() {
    return "welcome";
  }
}