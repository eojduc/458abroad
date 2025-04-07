package com.example.abroad.controller.admin;


import com.example.abroad.model.Application.PaymentStatus;
import com.example.abroad.service.page.admin.AdminApplicationInfoService.GetApplicationInfo;
import com.example.abroad.service.page.admin.AdminApplicationInfoService.PostNote.UserLacksPermission;
import com.example.abroad.service.page.admin.AdminApplicationInfoService.RefreshULink;
import com.example.abroad.service.page.admin.AdminApplicationInfoService.UpdatePaymentStatus;
import com.example.abroad.view.Alerts;
import com.example.abroad.service.UserService;
import com.example.abroad.service.page.admin.AdminApplicationInfoService;
import com.example.abroad.service.page.admin.AdminApplicationInfoService.GetApplicationInfo.ApplicationNotFound;
import com.example.abroad.service.page.admin.AdminApplicationInfoService.GetApplicationInfo.NotLoggedIn;
import com.example.abroad.service.page.admin.AdminApplicationInfoService.GetApplicationInfo.ProgramNotFound;
import com.example.abroad.service.page.admin.AdminApplicationInfoService.GetApplicationInfo.Success;
import com.example.abroad.service.page.admin.AdminApplicationInfoService.PostNote;
import com.example.abroad.service.page.admin.AdminApplicationInfoService.UpdateApplicationStatus;
import com.example.abroad.model.Application.Status;
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
                                             FormatService formatter, UserService userService) {

  @GetMapping("/admin/applications/{programId}/{username}")
  public String getApplicationInfo(@PathVariable String username, @PathVariable Integer programId, HttpSession session,
    Model model, @RequestParam Optional<String> error, @RequestParam Optional<String> success,
    @RequestParam Optional<String> warning, @RequestParam Optional<String> info) {
    return switch (service.getApplicationInfo(programId, username, session)) {
      case Success(var noteInfos, var documentInfos, var theme, var responses, var programDetails,
                   var applicationDetails, var displayName, var isReviewer, var requests, var isAdmin, var trackPayments, var preReqs) -> {
        model.addAllAttributes(Map.of(
          "theme", theme,
          // _application is used to avoid conflict with the application variable in Thymeleaf
          "formatter", formatter,
          "responses", responses,
          "alerts", new Alerts(error, success, warning, info),
          "noteInfos", noteInfos,
          "documentInfos", documentInfos,
          "programDetails", programDetails
        ));
        model.addAllAttributes(Map.of(
          "applicationDetails", applicationDetails,
          "programId", programId,
          "studentUsername", username,
          "displayName", displayName,
          "isReviewer", isReviewer,
          "isAdmin", isAdmin,
          "letterRequests", requests,
          "trackPayments", trackPayments,
          "preReqs", preReqs
        ));
        yield "admin/application-info :: page";
      }
      case ApplicationNotFound() -> "redirect:/admin/programs?error=That application does not exist";
      case GetApplicationInfo.UserLacksPermission() -> "redirect:/applications?error=You can't do that";
      case NotLoggedIn() -> "redirect:/login?error=You must be logged in to view this page";
      case ProgramNotFound() -> "redirect:/admin/programs?error=That program does not exist";
    };
  }

  @PostMapping("/admin/applications/{programId}/{username}/notes")
  public String createNote(@PathVariable String username, @PathVariable Integer programId,  HttpSession session,
    @RequestParam String content, Model model) {
    return switch (service.postNote(programId, username, content, session)) {
      case PostNote.Success(var notes) -> {
        model.addAllAttributes(Map.of(
          "noteInfos", notes,
          "formatter", formatter
        ));
        yield "admin/application-info :: note-table";
      }
      case PostNote.ApplicationNotFound() -> "redirect:/admin/programs?error=That application does not exist";
      case UserLacksPermission() -> "redirect:/applications?error=You are not an admin";
      case PostNote.NotLoggedIn() ->
        "redirect:/login?error=You must be logged in to view this page";
    };
  }


  @PostMapping("/admin/applications/{programId}/{username}/status")
  public String updateApplicationStatus(@PathVariable String username, @PathVariable Integer programId,
    HttpSession session, Model model,
    @RequestParam Status status) {
    switch (service.updateApplicationStatus(programId, username, status, session)) {
      case UpdateApplicationStatus.Success(var newStatus) -> {
        model.addAttribute("status", newStatus);
        return "components/statusBadge :: statusBadge";
      }
      case UpdateApplicationStatus.ApplicationNotFound() -> {
        model.addAttribute("status", "error");
        return "components/statusBadge :: statusBadge";
      }
      case UpdateApplicationStatus.NotLoggedIn() -> {
        model.addAttribute("status", "error");
        return "components/statusBadge :: statusBadge";
      }
      case UpdateApplicationStatus.UserNotAdmin() -> {
        model.addAttribute("status", "error");
        return "components/statusBadge :: statusBadge";
      }
      case UpdateApplicationStatus.UserLacksPermission() -> {
        model.addAttribute("status", "error");
        return "components/statusBadge :: statusBadge";
      }
      case UpdateApplicationStatus.ProgramNotFound() -> {
        model.addAttribute("status", "error");
        return "components/statusBadge :: statusBadge";
      }
    }
  }

  @PostMapping("/admin/applications/{programId}/{username}/payment-status")
  public String updatePaymentStatus(@PathVariable String username, @PathVariable Integer programId,
    HttpSession session, Model model,
    @RequestParam PaymentStatus status) {
    switch (service.updatePaymentStatus(session, status, programId, username)) {
      case UpdatePaymentStatus.Success(var newStatus) -> {
        model.addAttribute("status", newStatus);
        return "components/payment-status-badge :: payment-status-badge";
      }
      default -> {
        model.addAttribute("status", "error");
        return "components/payment-status-badge :: payment-status-badge";
      }
    }
  }

  @PostMapping("/admin/applications/{programId}/{username}/refresh-ulink")
  public String refreshULink(@PathVariable String username, @PathVariable Integer programId,
    HttpSession session) {
    return switch (service.refreshULink(session, username, programId)) {
      case RefreshULink.Success() -> String.format(
        "redirect:/admin/applications/%s/%s?success=ULink refreshed#prereqs",
        programId, username);
      case RefreshULink.NotLoggedIn() -> "redirect:/login?error=You must be logged in to view this page";
      case RefreshULink.UserNotFound() -> "redirect:/login?error=That Application does not exist";
      case RefreshULink.UserLacksPermission() -> "redirect:/applications?error=You can't do that";
      case RefreshULink.ConnectionError() -> "redirect:/admin/programs?error=Error connecting to uLink";
    };
  }

}
