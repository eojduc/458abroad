package com.example.abroad.controller.admin;

import com.example.abroad.view.Alerts;
import com.example.abroad.service.UserService;
import com.example.abroad.service.page.admin.AdminProgramInfoService;
import com.example.abroad.service.page.admin.AdminProgramInfoService.Column;
import com.example.abroad.service.page.admin.AdminProgramInfoService.DeleteProgram;
import com.example.abroad.service.page.admin.AdminProgramInfoService.DeleteProgram.ProgramNotFound;
import com.example.abroad.service.page.admin.AdminProgramInfoService.Filter;
import com.example.abroad.service.page.admin.AdminProgramInfoService.GetProgramInfo;
import com.example.abroad.service.FormatService;
import com.example.abroad.service.page.admin.AdminProgramInfoService.Sort;
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
public record AdminProgramInfoController(AdminProgramInfoService service, FormatService formatter,
                                         UserService userService) {

  @GetMapping("/admin/programs/{programId}")
  public String getProgramInfo(@PathVariable Integer programId, HttpSession session, Model model,
    @RequestParam Optional<String> error, @RequestParam Optional<String> success,
    @RequestParam Optional<String> warning, @RequestParam Optional<String> info) {
    return switch (service.getProgramInfo(programId, session, Optional.empty(), Optional.empty(), Optional.empty())) {
      case GetProgramInfo.UserNotFound() -> "redirect:/login?error=You are not logged in";
      case GetProgramInfo.UserNotAdmin() -> String.format("redirect:/programs/%s?error=You are not an admin", programId);
      case GetProgramInfo.ProgramNotFound() -> "redirect:/admin/programs?error=That program does not exist";
      case GetProgramInfo.UserLacksPermission() -> "redirect:/admin/programs?error=You do not have permission to view this program";
      case GetProgramInfo.Success(var program, var applicants, var user, var documentDeadlinePassed,
                                  var programisDone, var facultyLeads, var programDetails, var applicantDetails, var canSeeApplicants) -> {
        model.addAllAttributes(Map.of(
          "program", program,
          "applicants", applicants,
          "user", user,
          "formatter", formatter,
          "alerts", new Alerts(error, success, warning, info),
          "column", Column.NONE.name(),
          "filter", Filter.ALL.name(),
          "documentDeadlinePassed", documentDeadlinePassed,
          "programIsDone", programisDone,
          "programDetails", programDetails
        ));
        model.addAllAttributes(Map.of(
          "facultyLeads", facultyLeads,
          "applicantDetails", applicantDetails,
          "headers", applicantDetails.headers(),
          "canSeeApplicants", canSeeApplicants,
          "isAdmin", userService.isAdmin(user)
        ));
        yield "admin/program-info :: page";
      }
    };
  }

  @PostMapping("/admin/programs/{programId}/delete")
  public String deleteProgram(@PathVariable Integer programId, HttpSession session) {
    return switch (service.deleteProgram(programId, session)) {
      case DeleteProgram.Success() -> "redirect:/admin/programs?success=Program deleted";
      case DeleteProgram.UserNotFound() -> "redirect:/login?error=You are not logged in";
      case DeleteProgram.UserNotAdmin() -> "redirect:/programs?error=You are not an admin";
      case ProgramNotFound() -> "redirect:/admin/programs?error=That program does not exist";
      case DeleteProgram.UserLacksPermission() -> "redirect:/admin/programs?error=You do not have permission to delete this program";
    };
  }

  @GetMapping("/admin/programs/{programId}/applicants")
  public String getApplicantTable(@PathVariable Integer programId, HttpSession session,
    Model model, @RequestParam Optional<Column> column, @RequestParam Optional<Filter> filter,
    @RequestParam Optional<Sort> sort) {
    return switch (service.getProgramInfo(programId, session, column, filter, sort)) {
      case GetProgramInfo.UserNotFound() -> "redirect:/login?error=You are not logged in";
      case GetProgramInfo.UserNotAdmin() -> String.format("redirect:/programs/%s?error=You are not an admin", programId);
      case GetProgramInfo.ProgramNotFound() -> "redirect:/admin/programs?error=That program does not exist";
      case GetProgramInfo.UserLacksPermission() -> "redirect:/admin/programs?error=You do not have permission to view this program";
      case GetProgramInfo.Success(var program, var applicants, var user, var documentDeadlinePassed, var programIsDone,
                                  var facultyLeads, var programDetails, var applicantDetails, var canSeeApplicants) -> {
        model.addAllAttributes(Map.of(
          "program", program,
          "applicants", applicants,
          "formatter", formatter,
          "column", column.orElse(Column.NONE).name(),
          "filter", filter.orElse(Filter.ALL).name(),
          "sort", sort.orElse(Sort.ASCENDING).name(),
          "documentDeadlinePassed", documentDeadlinePassed,
          "programIsDone", programIsDone,
          "programDetails", programDetails,
          "applicantDetails", applicantDetails
        ));
        model.addAllAttributes(Map.of(
          "canSeeApplicants", canSeeApplicants,
            "isAdmin", userService.isAdmin(user)
        ));
        yield "admin/program-info :: applicant-table";
      }
    };
  }

}
