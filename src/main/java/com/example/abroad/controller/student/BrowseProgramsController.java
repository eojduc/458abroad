package com.example.abroad.controller.student;

import com.example.abroad.view.Alerts;
import com.example.abroad.service.FormatService;
import com.example.abroad.service.UserService;
import com.example.abroad.service.page.BrowseProgramsService;
import com.example.abroad.service.page.BrowseProgramsService.GetAllProgramsInfo;
import com.example.abroad.service.page.BrowseProgramsService.GetAllProgramsInfo.Success;
import com.example.abroad.service.page.BrowseProgramsService.GetAllProgramsInfo.UserNotFound;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public record BrowseProgramsController(BrowseProgramsService service, FormatService formatter,
                                       UserService userService) {

  @GetMapping("/programs")
  public String getProgramsInfo(HttpSession session, Model model,
      @RequestParam Optional<String> error,
      @RequestParam Optional<String> success,
      @RequestParam Optional<String> warning,
      @RequestParam Optional<String> info
  ) {

    GetAllProgramsInfo programsInfo = service.getProgramInfo(session, "", List.of());

    return switch (programsInfo) {
      case UserNotFound() -> "redirect:/login?error=You are not logged in";
      case Success(var programAndStatuses, var user) -> {

        model.addAllAttributes(
            Map.of(
                "user", user,
                "alerts", new Alerts(error, success, warning, info),
                "programAndStatuses", programAndStatuses,
                "knownFacultyLeads", service.getKnownFacultyLeads(),
                "formatter", formatter,
                "isNotStudent", !userService.isStudent(user)
            )
        );
        yield "student/programs :: page";
      }
      default -> throw new IllegalStateException("Unexpected value: " + programsInfo);
    };
  }

  @GetMapping("/programs/search")
  public String searchPrograms(HttpSession session, Model model,
      @RequestParam String nameFilter,
      @RequestParam(defaultValue = "") List<String> leadFilter
  ) {
    GetAllProgramsInfo programsInfo = service.getProgramInfo(session, nameFilter, leadFilter);

    return switch (programsInfo) {
      case UserNotFound() -> "redirect:/login?error=You are not logged in";
      case Success(var programsAndStatuses, var user) -> {
        model.addAllAttributes(
            Map.of(
                "user", user,
                "formatter", formatter,
                "programAndStatuses", programsAndStatuses,
                "knownFacultyLeads", service.getKnownFacultyLeads(),
                "isNotStudent", !userService.isStudent(user)
            )
        );
        yield "student/programs :: programTable";
      }
      default -> throw new IllegalStateException("Unexpected value: " + programsInfo);
    };
  }

}
