package com.example.abroad.controller;

import com.example.abroad.model.Alerts;
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

import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Controller
public record DashboardController(
    DashboardService dashboardService,
    FormatService formatter,
    SSOService ssoService) {
  @GetMapping("/")
  public String home(
      HttpServletRequest request,
      HttpSession session,
      Model model,
      @RequestParam Optional<String> error,
      @RequestParam Optional<String> success,
      @RequestParam Optional<String> warning,
      @RequestParam Optional<String> info) {

    if (dashboardService.getDashboard(session) instanceof DashboardService.GetDashboard.NotLoggedIn) {
      ssoService.authenticateSSO(request, session);
    }

    model.addAttribute("theme", dashboardService.getTheme(session));
    model.addAttribute("formatter", formatter);
    model.addAttribute("alerts", new Alerts(error, success, warning, info));

    return switch (dashboardService.getDashboard(session)) {
      case GetDashboard.NotLoggedIn() -> "homepage";
      case GetDashboard.StudentDashboard(var user) -> {
        model.addAttribute("displayName", user.displayName());
        model.addAttribute("student", user.username());
        model.addAttribute("user", user);
        yield "student/student-dashboard :: page";
      }
      case GetDashboard.AdminDashboard(var user) -> {
        model.addAttribute("displayName", user.displayName());
        model.addAttribute("user", user);
        yield "admin/admin-dashboard :: page";
      }
    };
  }

  @GetMapping("/ssoauthsession")
  public String home(HttpServletRequest request, Model model) {
    // Create a LinkedHashMap to preserve insertion order
    Map<String, String> headers = new LinkedHashMap<>();
    Enumeration<String> headerNames = request.getHeaderNames();
    while (headerNames.hasMoreElements()) {
      String headerName = headerNames.nextElement();
      String headerValue = request.getHeader(headerName);
      headers.put(headerName, headerValue);
    }
    model.addAttribute("headers", headers);
    return "home"; // Renders templates/headers.html
  }
}