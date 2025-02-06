package com.example.abroad.controller.admin;

import com.example.abroad.model.Alerts;
import com.example.abroad.model.Program.Semester;
import com.example.abroad.service.AddProgramService;
import com.example.abroad.service.AddProgramService.AddProgramInfo;
import com.example.abroad.service.AddProgramService.GetAddProgramInfo;
import com.example.abroad.service.EditProgramService;
import com.example.abroad.service.EditProgramService.GetEditProgramInfo;
import com.example.abroad.service.EditProgramService.UpdateProgramInfo;
import com.example.abroad.service.FormatService;
import com.example.abroad.service.UserService;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public record AddProgramPageController(AddProgramService service, FormatService formatter,
                                       UserService userService) {


  @GetMapping("/admin/programs/new")
  public String addProgramPage(HttpSession session,
      @RequestParam Optional<String> error, @RequestParam Optional<String> success,
      @RequestParam Optional<String> warning, @RequestParam Optional<String> info,
      Model model) {
    switch (service.getAddProgramInfo(session)) {
      case GetAddProgramInfo.Success(var user) -> {
        model.addAllAttributes(Map.of(
            "user", user,
            "alerts", new Alerts(error, success, warning, info),
            "formatter", formatter,
            "theme", userService.getTheme(session)
        ));
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
  public String addProgramPage(@RequestParam String title,
      @RequestParam String description,
      @RequestParam Integer year, @RequestParam LocalDate startDate, @RequestParam LocalDate endDate,
      @RequestParam String facultyLead,
      @RequestParam Semester semester, @RequestParam LocalDateTime applicationOpen,
      @RequestParam LocalDateTime applicationClose, HttpSession session) {
    return switch (service.addProgramInfo(title, description, year, startDate,
        endDate,
        facultyLead, semester, applicationOpen, applicationClose, session)) {
      case AddProgramInfo.Success(Integer programId) ->
          String.format("redirect:/admin/programs/%d?success=Program created", programId);
      case AddProgramInfo.NotLoggedIn() -> "redirect:/login?error=You are not logged in";
      case AddProgramInfo.UserNotAdmin() -> "redirect:/?error=You are not an admin";
    };
  }

}
