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
      @RequestParam(required = false) String sort,
      @RequestParam(required = false) String nameFilter,
      @RequestParam(required = false) String timeFilter,
      @RequestParam Optional<String> error,
      @RequestParam Optional<String> success,
      @RequestParam Optional<String> warning,
      @RequestParam Optional<String> info
      ) {
    boolean noSortOrFilter = sort == null && nameFilter == null && timeFilter == null;
    if (noSortOrFilter) {
      service.clearSessionData(session);
    }

    GetAllProgramsInfo programsInfo = service.getProgramInfo(session, sort, nameFilter, timeFilter, false);
    return switch (programsInfo) {
      case GetAllProgramsInfo.UserNotFound() -> "redirect:/login?error=You are not logged in";
      case GetAllProgramsInfo.UserNotAdmin() -> "redirect:/programs?error=You are not an admin";
      case GetAllProgramsInfo.Success(var programs, var applications, var user) -> {
        model.addAllAttributes(
            Map.of(
                "name", user.displayName(),
                "programs", programs,
                "programStatus", service.getProgramStatus(applications, programs),
                "sort", Objects.toString(session.getAttribute("lastSort"), ""),
              "alerts", new Alerts(error, success, warning, info),
              "formatter", formatter,
              "theme", userService.getTheme(session)
            )
        );
        yield "admin/programs :: " + ( noSortOrFilter ? "page" : "programTable");
      }
    };
  }


  @GetMapping("/admin/programs/sort")
  public String getProgramsInfo2(HttpSession session, Model model,
    @RequestParam(required = false) String sort,
    @RequestParam(required = false) String nameFilter,
    @RequestParam(required = false) String timeFilter,
    @RequestParam Optional<String> error,
    @RequestParam Optional<String> success,
    @RequestParam Optional<String> warning,
    @RequestParam Optional<String> info
  ) {
    boolean noSortOrFilter = sort == null && nameFilter == null && timeFilter == null;
    if (noSortOrFilter) {
      service.clearSessionData(session);
    }

    GetAllProgramsInfo programsInfo = service.getProgramInfo(session, sort, nameFilter, timeFilter, false);
    return switch (programsInfo) {
      case GetAllProgramsInfo.UserNotFound() -> "redirect:/login?error=You are not logged in";
      case GetAllProgramsInfo.UserNotAdmin() -> "redirect:/programs?error=You are not an admin";
      case GetAllProgramsInfo.Success(var programs, var applications, var user) -> {
        model.addAllAttributes(
          Map.of(
            "name", user.displayName(),
            "programs", programs,
            "programStatus", service.getProgramStatus(applications, programs),
            "sort", Objects.toString(session.getAttribute("lastSort"), ""),
            "alerts", new Alerts(error, success, warning, info),
            "formatter", formatter,
            "theme", userService.getTheme(session)
          )
        );
        yield "admin/programs :: " + ( noSortOrFilter ? "page" : "programTable");
      }
    };
  }

}
