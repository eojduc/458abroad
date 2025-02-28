package com.example.abroad.controller.admin;

import com.example.abroad.model.Alerts;
import com.example.abroad.model.Program.Semester;
import com.example.abroad.service.FormatService;
import com.example.abroad.service.UserService;
import com.example.abroad.service.page.AddProgramService;
import com.example.abroad.service.page.AddProgramService.AddProgramInfo;
import com.example.abroad.service.page.AddProgramService.GetAddProgramInfo;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public record AddProgramPageController(AddProgramService service, FormatService formatter,
                                       UserService userService) {

  static Logger logger = LoggerFactory.getLogger(AddProgramPageController.class);


  @GetMapping("/admin/programs/new")
  public String addProgramPage(HttpSession session,
      @RequestHeader(value = "Referer", required = false) String referer,
      @RequestParam Optional<String> error, @RequestParam Optional<String> success,
      @RequestParam Optional<String> warning, @RequestParam Optional<String> info, Model model) {
    switch (service.getAddProgramInfo(session)) {
      case GetAddProgramInfo.Success(var user) -> {
        model.addAllAttributes(
            Map.of("user", user,
                "referer", Optional.ofNullable(referer).orElse("/admin/programs"),
                "alerts", new Alerts(error, success, warning, info),
                "formatter", formatter,
                "adminList", service.getAdminList()));
        return "admin/add-program :: page";
      }
      case GetAddProgramInfo.NotLoggedIn() -> {
        return "redirect:/login?error=You are not logged in";
      }
      case GetAddProgramInfo.UserNotAdmin() -> {
        return "redirect:/programs?error=You are not an admin";
      }
    }
  }

  @PostMapping("/admin/programs/new")
  public String addProgramPage(@RequestParam String title, @RequestParam String description,
      @RequestParam Integer year, @RequestParam LocalDate startDate,
      @RequestParam LocalDate endDate,
      @RequestParam Semester semester, @RequestParam LocalDate applicationOpen,
      @RequestParam LocalDate applicationClose, HttpSession session, Model model) {
    return switch (service.addProgramInfo(title, description, year, startDate, endDate,
        semester, applicationOpen, applicationClose, session)) {
      case AddProgramInfo.Success(Integer programId) ->
          String.format("redirect:/admin/programs/%d?success=Program created", programId);
      case AddProgramInfo.NotLoggedIn() -> "redirect:/login?error=You are not logged in";
      case AddProgramInfo.UserNotAdmin() -> "redirect:/?error=You are not an admin";
      case AddProgramInfo.InvalidProgramInfo(var message) -> {
        model.addAttribute("alerts",
            new Alerts(Optional.of(message), Optional.empty(), Optional.empty(), Optional.empty()));
        yield "components :: alerts";
      }
      case AddProgramInfo.DatabaseError(var message) -> {
        logger.error("Error saving program: {}", message);
        model.addAttribute("alerts",
            new Alerts(Optional.of("An unknown error occurred"), Optional.empty(), Optional.empty(),
                Optional.empty()));
        yield "components :: alerts";
      }
    };
  }

}
