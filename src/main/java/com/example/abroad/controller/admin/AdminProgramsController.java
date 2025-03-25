package com.example.abroad.controller.admin;

import static java.util.Map.entry;

import com.example.abroad.view.Alerts;
import com.example.abroad.service.FormatService;
import com.example.abroad.service.UserService;
import com.example.abroad.service.page.admin.AdminProgramsService;
import com.example.abroad.service.page.admin.AdminProgramsService.GetAllProgramsInfo;
import com.example.abroad.service.page.admin.AdminProgramsService.GetAllProgramsInfo.Success;
import com.example.abroad.service.page.admin.AdminProgramsService.GetAllProgramsInfo.UserNotAdmin;
import com.example.abroad.service.page.admin.AdminProgramsService.GetAllProgramsInfo.UserNotFound;
import com.example.abroad.service.page.admin.AdminProgramsService.Sort;
import com.example.abroad.service.page.admin.AdminProgramsService.TimeFilter;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public record AdminProgramsController(AdminProgramsService service, FormatService formatter,
                                      UserService userService) {

  @GetMapping("/admin/programs")
  public String getProgramsInfo(
      HttpSession session, Model model,
      @RequestParam Optional<String> error,
      @RequestParam Optional<String> success,
      @RequestParam Optional<String> warning,
      @RequestParam Optional<String> info
  ) {
    GetAllProgramsInfo programsInfo = service.getProgramInfo(session, Sort.TITLE, "", List.of(),
        TimeFilter.FUTURE, true);
    return switch (programsInfo) {
      case UserNotFound() -> "redirect:/login?error=You are not logged in";
      case UserNotAdmin() -> "redirect:/programs?error=You are not an admin";
      case Success(var programAndStatuses, var user) -> {
        model.addAllAttributes(
            Map.ofEntries(
                entry("name", user.displayName()),
                entry("knownFacultyLeads", service.getKnownFacultyLeads()),
                entry("sort", Sort.TITLE.name()),
                entry("alerts", new Alerts(error, success, warning, info)),
                entry("formatter", formatter),
                entry("nameFilter", ""),
                entry("leadFilter", List.of()),
                entry("timeFilter", TimeFilter.FUTURE.name()),
                entry("user", user),
                entry("ascending", true),
                entry("programAndStatuses", programAndStatuses)
            )
        );
        yield "admin/programs :: page";
      }
    };
  }

  @GetMapping("/admin/programs/table")
  public String getProgramsInfo3(HttpSession session, Model model,
      @RequestParam Sort sort,
      @RequestParam String nameFilter,
      @RequestParam(defaultValue = "") List<String> leadFilter,
      @RequestParam TimeFilter timeFilter,
      @RequestParam Boolean ascending
  ) {

    GetAllProgramsInfo programsInfo = service.getProgramInfo(session, sort, nameFilter, leadFilter,
        timeFilter, ascending);
    return switch (programsInfo) {
      case UserNotFound() -> "redirect:/login?error=You are not logged in";
      case UserNotAdmin() -> "redirect:/programs?error=You are not an admin";
      case Success(var programAndStatuses, var user) -> {
        model.addAllAttributes(
            Map.ofEntries(
                entry("name", user.displayName()),
                entry("knownFacultyLeads", service.getKnownFacultyLeads()),
                entry("sort", sort.name()),
                entry("nameFilter", nameFilter),
                entry("leadFilter", leadFilter),
                entry("timeFilter", timeFilter.name()),
                entry("formatter", formatter),
                entry("ascending", ascending),
                entry("programAndStatuses", programAndStatuses)
            )
        );
        yield "admin/programs :: programTable";
      }
    };
  }

}
