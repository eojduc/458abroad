package com.example.abroad.controller.admin;

import com.example.abroad.model.Alerts;
import com.example.abroad.service.FormatService;
import com.example.abroad.service.UserService;
import com.example.abroad.service.page.admin.AdminProgramsService;
import com.example.abroad.service.page.admin.AdminProgramsService.GetAllProgramsInfo;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public record AdminProgramsController(AdminProgramsService service, FormatService formatter,
                                      UserService userService) {

  @GetMapping("/admin/programs")
  public String getProgramsInfo(HttpSession session, Model model,
      @RequestParam Optional<String> error,
      @RequestParam Optional<String> success,
      @RequestParam Optional<String> warning,
      @RequestParam Optional<String> info
      ) {
    GetAllProgramsInfo programsInfo = service.getProgramInfo(session, "title", "", "future", false, true);
    return switch (programsInfo) {
      case GetAllProgramsInfo.UserNotFound() -> "redirect:/login?error=You are not logged in";
      case GetAllProgramsInfo.UserNotAdmin() -> "redirect:/programs?error=You are not an admin";
      case GetAllProgramsInfo.Success(var programs, var applications, var user) -> {
        model.addAllAttributes(
            Map.of(
                "name", user.displayName(),
                "programs", programs,
                "programStatus", service.getProgramStatus(applications, programs),
                "sort", "title",
              "alerts", new Alerts(error, success, warning, info),
              "formatter", formatter,
              "nameFilter", "",
              "theme", userService.getTheme(session),
              "ascending", true
            )
        );
        yield "admin/programs :: page";
      }
    };
  }
  @GetMapping("/admin/programs/table")
  public String getProgramsInfo3(HttpSession session, Model model,
    @RequestParam String sort,
    @RequestParam String nameFilter,
    @RequestParam String timeFilter,
    @RequestParam Boolean ascending,
    @RequestParam Optional<String> error,
    @RequestParam Optional<String> success,
    @RequestParam Optional<String> warning,
    @RequestParam Optional<String> info
  ) {

    GetAllProgramsInfo programsInfo = service.getProgramInfo(session, sort, nameFilter, timeFilter, false, true);
    return switch (programsInfo) {
      case GetAllProgramsInfo.UserNotFound() -> "redirect:/login?error=You are not logged in";
      case GetAllProgramsInfo.UserNotAdmin() -> "redirect:/programs?error=You are not an admin";
      case GetAllProgramsInfo.Success(var programs, var applications, var user) -> {
        model.addAllAttributes(
          Map.of(
            "name", user.displayName(),
            "programs", programs,
            "programStatus", service.getProgramStatus(applications, programs),
            "sort", sort,
            "nameFilter", nameFilter,
            "timeFilter", timeFilter,
            "alerts", new Alerts(error, success, warning, info),
            "formatter", formatter,
            "theme", userService.getTheme(session),
            "ascending", ascending
          )
        );
        yield "admin/programs :: programTable";
      }
    };
  }

}
