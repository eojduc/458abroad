package com.example.abroad.controller.admin;

import com.example.abroad.view.Alerts;
import com.example.abroad.service.page.DashboardService;
import com.example.abroad.service.page.SSOService;
import com.example.abroad.service.page.DashboardService.GetDashboard;
import com.example.abroad.service.FormatService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public record PreviewPageController(
    DashboardService dashboardService,
    FormatService formatter,
    SSOService ssoService) {
  @GetMapping("/preview")
  public String home(
      HttpServletRequest request,
      HttpSession session,
      Model model,
      @RequestParam Optional<String> error,
      @RequestParam Optional<String> success,
      @RequestParam Optional<String> warning,
      @RequestParam Optional<String> info) {
    model.addAttribute("formatter", formatter);
    model.addAttribute("alerts", new Alerts(error, success, warning, info));
    return switch (dashboardService.getDashboard(session, request)) {
      case GetDashboard.NotLoggedIn() -> "homepage";
      case GetDashboard.StudentDashboard(var user) -> {
        model.addAttribute("user", user);
        model.addAttribute("isAdmin", false);
        yield "student/student-dashboard-preview :: page";
      }
      case GetDashboard.AdminDashboard(var user, var isAdmin, var isHeadAdmin) -> {
        model.addAttribute("user", user);
        model.addAttribute("isAdmin", isAdmin);
        model.addAttribute("isHeadAdmin", isHeadAdmin);
        yield "admin/admin-dashboard-preview :: page";
      }
      case GetDashboard.SSOUsernameTaken(var redirect) -> "redirect:" + redirect;
    };
  }
}