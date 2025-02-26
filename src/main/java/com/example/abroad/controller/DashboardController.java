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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
        SSOService.SSOResult ssoResult = ssoService.authenticateSSO(request, session);
        if (ssoResult instanceof SSOService.SSOResult.UsernameTaken usernameTaken) {
            String errorMessage = usernameTaken.message();
            String encodedError = URLEncoder.encode(errorMessage, StandardCharsets.UTF_8);
            String returnUrl = URLEncoder.encode("https://beta.colab.duke.edu/register?error=" + encodedError, StandardCharsets.UTF_8);
            return "redirect:/Shibboleth.sso/Logout?return=https://shib.oit.duke.edu/cgi-bin/logout.pl?logoutWithoutPrompt=1&returnto=" + returnUrl;
        }
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
}