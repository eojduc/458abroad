package com.example.abroad.controller.student;

import com.example.abroad.service.FormatService;
import com.example.abroad.service.ViewApplicationService;
import com.example.abroad.service.ViewApplicationService.GetApplicationResult;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import jakarta.servlet.http.HttpSession;

@Controller
public record ViewApplicationController(ViewApplicationService applicationService, FormatService formatter) {

  @GetMapping("/applications/{applicationId}")
  public String viewApplication(@PathVariable String applicationId, HttpSession session, Model model) {
    GetApplicationResult result = applicationService.getApplication(applicationId, session);
    return switch (result) {
      case GetApplicationResult.Success(var app, var user) -> {
        model.addAllAttributes(Map.of(
          "app", app,
          "user", user,
          "formatter", formatter
        ));
        yield "student/view-application :: page";
      }
      case GetApplicationResult.UserNotFound() -> "redirect:/login?error=Not logged in";
      case GetApplicationResult.ApplicationNotFound() -> "redirect:/dashboard?error=Application not found";
      case GetApplicationResult.AccessDenied() -> "redirect:/dashboard?error=Access denied";
    };
  }
}