package com.example.abroad.controller.student;

import com.example.abroad.model.Application;
import com.example.abroad.service.FormatService;
import com.example.abroad.service.UserService;
import com.example.abroad.service.ViewApplicationService;

import java.time.Instant;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import jakarta.servlet.http.HttpSession;

@Controller
public record ViewApplicationController(
    ViewApplicationService applicationService,
    FormatService formatter,
    UserService userService) {

  @GetMapping("/applications/{applicationId}")
  public String viewApplication(@PathVariable String applicationId, HttpSession session, Model model) {
    var result = applicationService.getApplication(applicationId, session);
    return switch (result) {
      case ViewApplicationService.GetApplicationResult.Success(var app, var prog, var user) -> {
        boolean editable = false;
        if (app.status().equals(Application.Status.APPLIED)) {
          Instant now = Instant.now();
          if (now.isAfter(prog.applicationOpen()) && now.isBefore(prog.applicationClose())) {
            editable = true;
          }
        }
        model.addAllAttributes(Map.of(
            "app", app,
            "prog", prog,
            "user", user,
            "formatter", formatter,
            "theme", userService.getTheme(session),
            "editable", editable));
        yield "student/view-application :: page";
      }
      case ViewApplicationService.GetApplicationResult.UserNotFound() -> "redirect:/login?error=Not logged in";
      case ViewApplicationService.GetApplicationResult.ApplicationNotFound() ->
        "redirect:/?error=Application not found";
      case ViewApplicationService.GetApplicationResult.AccessDenied() -> "redirect:/?error=Access denied";
      case ViewApplicationService.GetApplicationResult.ProgramNotFound() ->
        "redirect:/?error=Program not found";
    };
  }
}
