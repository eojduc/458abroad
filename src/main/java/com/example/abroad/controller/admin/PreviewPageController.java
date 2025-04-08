package com.example.abroad.controller.admin;

import com.example.abroad.service.UserService;
import com.example.abroad.view.Alerts;
import com.example.abroad.service.page.SSOService;
import com.example.abroad.service.FormatService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
public record PreviewPageController(
    UserService userService,
    FormatService formatter,
    SSOService ssoService) {

  @GetMapping("/preview")
  public String defaultPreview(
      Model model,
      @RequestParam Optional<String> error,
      @RequestParam Optional<String> success,
      @RequestParam Optional<String> warning,
      @RequestParam Optional<String> info) {

    return previewSpecificPage("admin", model, error, success, warning, info);
  }

  @GetMapping("/preview/{page}")
  public String previewSpecificPage(
      @PathVariable String page,
      Model model,
      @RequestParam Optional<String> error,
      @RequestParam Optional<String> success,
      @RequestParam Optional<String> warning,
      @RequestParam Optional<String> info) {

    model.addAttribute("formatter", formatter);
    model.addAttribute("user", userService.previewUser());
    model.addAttribute("alerts", new Alerts(error, success, warning, info));

    return switch (page) {
      case "admin" -> "admin/admin-dashboard-preview :: page";
      case "student" -> "student/student-dashboard-preview :: page";
      case "home" -> "home-page-preview :: page";
      default -> "admin/admin-dashboard-preview :: page";
    };
  }
}
