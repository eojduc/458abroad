package com.example.abroad.controller.student;

import com.example.abroad.model.Alerts;
import com.example.abroad.model.Application;
import com.example.abroad.model.Program;
import com.example.abroad.service.FormatService;
import com.example.abroad.service.UserService;
import com.example.abroad.service.page.admin.AdminProgramsService;
import com.example.abroad.service.page.admin.AdminProgramsService.GetAllProgramsInfo;
import com.example.abroad.service.page.admin.AdminProgramsService.GetAllProgramsInfo.Success;
import com.example.abroad.service.page.admin.AdminProgramsService.GetAllProgramsInfo.UserNotFound;
import jakarta.servlet.http.HttpSession;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public record BrowseProgramsController(AdminProgramsService service, FormatService formatter,
                                       UserService userService) {

  private static final Logger logger = LoggerFactory.getLogger(BrowseProgramsController.class);

  @GetMapping("/programs")
  public String getProgramsInfo(HttpSession session, Model model,
      @RequestParam(required = false) String nameFilter,
      @RequestParam Optional<String> error,
      @RequestParam Optional<String> success,
      @RequestParam Optional<String> warning,
      @RequestParam Optional<String> info
  ) {
    String sort = "appCloses";
    String timeFilter = "future";
    boolean noFilter = nameFilter == null;
    if (noFilter) {
      service.clearSessionData(session);
    }

    GetAllProgramsInfo programsInfo = service.getProgramInfo(session, sort, nameFilter, timeFilter,
        true, true);

    return switch (programsInfo) {
      case UserNotFound() -> "redirect:/login?error=You are not logged in";
      case Success(var programs, var applications, var user) -> {

        logger.info("User {} \nApplications {} \n", user, applications);
        // Create the ordered list that will contain the application statuses
        List<String> programStatus = new ArrayList<>(Collections.nCopies(programs.size(), null));

// Populate status for each program position
        for (int i = 0; i < programs.size(); i++) {
          Program program = programs.get(i);
          LocalDate today = LocalDate.now();
          Instant nowInstant = Instant.now();

          // Find if there's an application for this program
          Application userApp = applications.stream()
              .filter(app -> Objects.equals(app.student(), user.username())
                  && Objects.equals(app.programId(), program.id()))
              .findFirst()
              .orElse(null);

          if (userApp != null) {
            // If user has an application, use its status
            programStatus.set(i, userApp.status().toString());
          } else {
            // No application - determine why
            if (today.isBefore(program.applicationOpen())) {
              programStatus.set(i, "NotOpen");
            } else if (today.isAfter(program.applicationClose())) {
              programStatus.set(i, "DeadPass");
            } else {
              programStatus.set(i, "Open");
            }
          }
        }

        model.addAllAttributes(
            Map.of(
                "user", user,
                "programs", programs,
                "programStatus", programStatus,
                "alerts", new Alerts(error, success, warning, info),
                "formatter", formatter,
              "theme", userService.getTheme(session)
            )
        );
        yield "student/programs :: " + (noFilter ? "page" : "programTable");
      }
      default -> throw new IllegalStateException("Unexpected value: " + programsInfo);
    };
  }

}
