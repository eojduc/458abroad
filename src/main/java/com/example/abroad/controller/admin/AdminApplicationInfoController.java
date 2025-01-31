package com.example.abroad.controller.admin;


import com.example.abroad.model.Alerts;
import com.example.abroad.model.Application;
import com.example.abroad.model.Question;
import com.example.abroad.service.AdminApplicationInfoService;
import com.example.abroad.service.AdminApplicationInfoService.GetApplicationInfo;
import com.example.abroad.service.AdminApplicationInfoService.UpdateApplicationStatus;
import com.example.abroad.service.FormatService;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public record AdminApplicationInfoController(AdminApplicationInfoService service,
                                             FormatService formatter) {

  @GetMapping("/admin/applications/{applicationId}")
  public String getApplicationInfo(@PathVariable String applicationId, HttpSession session,
    Model model, @RequestParam Optional<String> error, @RequestParam Optional<String> success,
    @RequestParam Optional<String> warning, @RequestParam Optional<String> info) {
    return switch (service.getApplicationInfo(applicationId, session)) {
      case GetApplicationInfo.Success(var program, var student, var application, var user) -> {
        model.addAllAttributes(Map.of(
          "program", program,
          "student", student,
          "_application", application,
          // _application is used to avoid conflict with the application variable in Thymeleaf
          "formatter", formatter,
          "questions", Question.QUESTIONS,
          "user", user,
          "alerts", new Alerts(error, success, warning, info)
        ));
        yield "admin/application-info :: page";
      }
      case GetApplicationInfo.ApplicationNotFound() -> "redirect:/admin/programs?error=That application does not exist";
      case GetApplicationInfo.UserNotAdmin() -> "redirect:/applications?error=You are not an admin";
      case GetApplicationInfo.NotLoggedIn() ->
        "redirect:/login?error=You must be logged in to view this page";
    };
  }


  @PostMapping("/admin/applications/{applicationId}/status")
  public String updateApplicationStatus(@PathVariable String applicationId,
    HttpSession session, Model model,
    @RequestParam Application.Status status) {
    switch (service.updateApplicationStatus(applicationId, status, session)) {
      case UpdateApplicationStatus.Success(var newStatus) -> {
        model.addAttribute("status", newStatus.name());
        return "components :: statusBadge";
      }
      case UpdateApplicationStatus.ApplicationNotFound() -> {
        model.addAttribute("status", "error");
        return "components :: statusBadge";
      }
      case UpdateApplicationStatus.NotLoggedIn() -> {
        model.addAttribute("status", "error");
        return "components :: statusBadge";
      }
      case UpdateApplicationStatus.UserNotAdmin() -> {
        model.addAttribute("status", "error");
        return "components :: statusBadge";
      }
    }
  }

}
