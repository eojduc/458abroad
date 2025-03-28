package com.example.abroad.controller.admin;

import com.example.abroad.view.Alerts;
import com.example.abroad.model.Program.Semester;
import com.example.abroad.service.FormatService;
import com.example.abroad.service.UserService;
import com.example.abroad.service.page.admin.AddProgramService;
import com.example.abroad.service.page.admin.AddProgramService.AddProgramInfo;
import com.example.abroad.service.page.admin.AddProgramService.GetAddProgramInfo;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.List;
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
                "defaultQuestions", service.getDefaultQuestions(),
                "adminList", service.getFacultyList()));
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
    public String addProgramPage(
        @RequestParam String title,
        @RequestParam String description,
        @RequestParam List<String> facultyLeads,
        @RequestParam LocalDate essentialDocsDate,
        @RequestParam Integer year,
        @RequestParam LocalDate startDate,
        @RequestParam LocalDate endDate,
        @RequestParam Semester semester,
        @RequestParam LocalDate applicationOpen,
        @RequestParam LocalDate applicationClose,
        @RequestParam(required = false) List<String> selectedQuestions,
        HttpSession session,
        Model model,
        HttpServletResponse response) {

      return switch (service.addProgramInfo(title, description, facultyLeads, year, startDate, endDate,
          essentialDocsDate, semester, applicationOpen, applicationClose, selectedQuestions, session)) {
        case AddProgramInfo.Success(Integer programId) -> {
        response.setHeader("HX-Redirect", String.format("/admin/programs/%d?success=Program created", programId));
        yield "components :: empty";  // Return a minimal fragment
      }
      case AddProgramInfo.NotLoggedIn() -> {
        response.setHeader("HX-Redirect", "/login?error=You are not logged in");
        yield "components :: empty";  // Return a minimal fragment
      }
      case AddProgramInfo.UserNotAdmin() -> {
        response.setHeader("HX-Redirect", "/?error=You are not an admin");
        yield "components :: empty";  // Return a minimal fragment
      }
      case AddProgramInfo.InvalidProgramInfo(var message) -> {
        model.addAttribute("alerts", new Alerts(Optional.of(message), Optional.empty(), Optional.empty(), Optional.empty()));
        yield "components :: alerts";  // Return the alerts fragment
      }
      case AddProgramInfo.DatabaseError(var message) -> {
        logger.error("Error saving program: {}", message);
        model.addAttribute("alerts", new Alerts(Optional.of("An unknown error occurred"), Optional.empty(), Optional.empty(), Optional.empty()));
        yield "components :: alerts";  // Return the alerts fragment
      }
    };
  }
}
