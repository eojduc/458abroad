package com.example.abroad.controller.admin;


import com.example.abroad.model.Alerts;
import com.example.abroad.model.Application.Status;
import com.example.abroad.service.UserService;
import com.example.abroad.service.page.admin.AdminApplicationInfoService;
import com.example.abroad.service.page.admin.AdminApplicationInfoService.GetApplicationInfo.ApplicationNotFound;
import com.example.abroad.service.page.admin.AdminApplicationInfoService.GetApplicationInfo.NotLoggedIn;
import com.example.abroad.service.page.admin.AdminApplicationInfoService.GetApplicationInfo.ProgramNotFound;
import com.example.abroad.service.page.admin.AdminApplicationInfoService.GetApplicationInfo.Success;
import com.example.abroad.service.page.admin.AdminApplicationInfoService.GetApplicationInfo.UserNotAdmin;
import com.example.abroad.service.page.admin.AdminApplicationInfoService.PostNote;
import com.example.abroad.service.page.admin.AdminApplicationInfoService.UpdateApplicationStatus;
import com.example.abroad.service.FormatService;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
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
                                             FormatService formatter, UserService userService) {

  @GetMapping("/admin/applications/{applicationId}")
  public String getApplicationInfo(@PathVariable String applicationId, HttpSession session,
    Model model, @RequestParam Optional<String> error, @RequestParam Optional<String> success,
    @RequestParam Optional<String> warning, @RequestParam Optional<String> info) {
    return switch (service.getApplicationInfo(applicationId, session)) {
      case Success(var program, var student, var application, var user, var notes, var documents, var status, var facultyLeads, var responses) -> {
        model.addAllAttributes(Map.of(
          "program", program,
          "programIsPast", program.endDate().isBefore(LocalDate.now()),
          "student", student,
          "_application", application,
          // _application is used to avoid conflict with the application variable in Thymeleaf
          "formatter", formatter,
          "responses", responses,
          "user", user,
          "alerts", new Alerts(error, success, warning, info),
          "theme", userService.getTheme(session),
          "notes", notes
        ));
        model.addAllAttributes(Map.of(
          "documents", documents,
          "status", status,
          "facultyLeads", facultyLeads
        ));
        yield "admin/application-info :: page";
      }
      case ApplicationNotFound() -> "redirect:/admin/programs?error=That application does not exist";
      case UserNotAdmin() -> "redirect:/applications?error=You are not an admin";
      case NotLoggedIn() ->
        "redirect:/login?error=You must be logged in to view this page";
      case ProgramNotFound() -> "redirect:/admin/programs?error=That program does not exist";
    };
  }

  @PostMapping("/admin/applications/{applicationId}/notes")
  public String createNote(@PathVariable String applicationId, HttpSession session,
    @RequestParam String content, Model model) {
    return switch (service.postNote(applicationId, content, session)) {
      case PostNote.Success(var notes) -> {
        model.addAllAttributes(Map.of(
          "notes", notes,
          "formatter", formatter
        ));
        yield "admin/application-info :: note-table";
      }
      case PostNote.ApplicationNotFound() -> "redirect:/admin/programs?error=That application does not exist";
      case PostNote.UserNotAdmin() -> "redirect:/applications?error=You are not an admin";
      case PostNote.NotLoggedIn() ->
        "redirect:/login?error=You must be logged in to view this page";
    };
  }


  @PostMapping("/admin/applications/{applicationId}/status")
  public String updateApplicationStatus(@PathVariable String applicationId,
    HttpSession session, Model model,
    @RequestParam Status status) {
    switch (service.updateApplicationStatus(applicationId, status, session)) {
      case UpdateApplicationStatus.Success(var newStatus) -> {
        model.addAttribute("status", newStatus);
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
      case UpdateApplicationStatus.ProgramNotFound() -> {
        model.addAttribute("status", "error");
        return "components :: statusBadge";
      }
    }
  }

}
