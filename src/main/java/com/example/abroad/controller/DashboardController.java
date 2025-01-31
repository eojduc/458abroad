package com.example.abroad.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
public class DashboardController {
  @GetMapping("/dashboard")
  public String showDashboard(HttpSession session, Model model) {
    String displayName = (String) session.getAttribute("displayName");
    String username = (String) session.getAttribute("username");
    System.out.println(
      "Getting username from session: " + session.getAttribute("username"));
    model.addAttribute("displayName", displayName);
    model.addAttribute("student", username); // Add this for navbar
    return "dashboard/student-dashboard :: page";
  }

  @GetMapping("/admin/dashboard")
  public String adminDashboard(HttpServletRequest request, Model model) {
    var displayName = Optional.ofNullable(request.getSession().getAttribute("displayName"))
      .filter(obj -> obj instanceof String)
      .map(obj -> (String) obj)
      .orElse("Admin"); // Default fallback

    model.addAttribute("displayName", displayName);

    return "dashboard/admin-dashboard :: page";  // Note the :: page suffix
  }
}