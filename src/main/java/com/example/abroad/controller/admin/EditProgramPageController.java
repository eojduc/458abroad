package com.example.abroad.controller.admin;

import com.example.abroad.service.EditProgramService;
import com.example.abroad.service.EditProgramService.GetEditProgramInfo;
import com.example.abroad.service.EditProgramService.UpdateProgramInfo;
import com.example.abroad.model.Alerts;
import com.example.abroad.model.Program.Semester;
import com.example.abroad.service.ISOFormatService;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public record EditProgramPageController(EditProgramService service, ISOFormatService formatter) {


  @GetMapping("/admin/programs/{programId}/edit")
  public String editProgramPage(HttpServletRequest request, @PathVariable Integer programId,
    @RequestParam Optional<String> error, @RequestParam Optional<String> success,
    @RequestParam Optional<String> warning, @RequestParam Optional<String> info) {
    switch(service.getEditProgramInfo(programId, request)) {
      case GetEditProgramInfo.Success(var program, var user) -> {
        request.setAttribute("program", program);
        request.setAttribute("user", user);
        request.setAttribute("alerts", new Alerts(error, success, warning, info));
        request.setAttribute("formatter", formatter);
        return "admin/edit-program :: page";
      }
      case GetEditProgramInfo.NotLoggedIn() -> {
        return "redirect:/login?error=You are not logged in";
      }
      case GetEditProgramInfo.UserNotAdmin() -> {
        return "redirect:/admin/programs?error=You are not an admin";
      }
      case GetEditProgramInfo.ProgramNotFound() -> {
        return "redirect:/admin/programs?error=That program does not exist";
      }
    }
  }

  @PostMapping("/admin/programs/{programId}/edit")
  public String editProgramPost(@PathVariable Integer programId, @RequestParam String title, @RequestParam String description,
    @RequestParam Integer year, @RequestParam LocalDate startDate, @RequestParam LocalDate endDate,
    @RequestParam String facultyLead,
    @RequestParam Semester semester, @RequestParam LocalDateTime applicationOpen,
    @RequestParam LocalDateTime applicationClose, HttpServletRequest request) {
    return switch(service.updateProgramInfo(programId, title, description, year, startDate, endDate,
      facultyLead, semester, applicationOpen, applicationClose, request)) {
      case UpdateProgramInfo.Success() ->
        String.format("redirect:/admin/programs/%d/edit?success=Program updated", programId);

      case UpdateProgramInfo.NotLoggedIn() -> "redirect:/login?error=You are not logged in";
      case UpdateProgramInfo.UserNotAdmin() ->
        "redirect:/?error=You are not an admin";
      case UpdateProgramInfo.ProgramNotFound() ->
        "redirect:/admin/programs?error=That program does not exist";
    };
  }

}
