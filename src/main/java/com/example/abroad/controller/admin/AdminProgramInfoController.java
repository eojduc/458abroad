package com.example.abroad.controller.admin;

import com.example.abroad.service.AdminProgramInfoService;
import com.example.abroad.service.AdminProgramInfoService.DeleteProgram;
import com.example.abroad.service.AdminProgramInfoService.DeleteProgram.ProgramNotFound;
import com.example.abroad.service.AdminProgramInfoService.GetProgramInfo;
import com.example.abroad.service.FormatService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;


@Controller
public record AdminProgramInfoController(AdminProgramInfoService service, FormatService formatter) {

  @GetMapping("/admin/programs/{programId}")
  public String getProgramInfo(@PathVariable Integer programId, HttpServletRequest request, Model model) {
    switch (service.getProgramInfo(programId, request)) {
      case GetProgramInfo.UserNotFound() -> {
        return "redirect:/login?error=You are not logged in";
      }
      case GetProgramInfo.UserNotAdmin() -> {
        return String.format("redirect:/programs/%s?error=You are not an admin", programId);
      }
      case GetProgramInfo.ProgramNotFound(var user) -> {
        model.addAllAttributes(Map.of(
          "title", "Program not found",
          "message", "That program does not exist"
        ));
        return "error :: custom-page";
      }
      case GetProgramInfo.Success(var program, var applicants, var user) -> {
        model.addAllAttributes(Map.of(
          "program", program,
          "applicants", applicants,
          "user", user,
          "formatter", formatter
        ));
        return "admin/program-info :: page";
      }
    }
  }

  @PostMapping("/admin/programs/{programId}/delete")
  public String deleteProgram(@PathVariable Integer programId, HttpServletRequest request) {
    return switch (service.deleteProgram(programId, request)) {
      case DeleteProgram.Success() ->
        "redirect:/admin/programs?success=Program deleted";
      case DeleteProgram.UserNotFound() ->
        "redirect:/login?error=You are not logged in";
      case DeleteProgram.UserNotAdmin() ->
        String.format("redirect:/programs/%s?error=You are not an admin", programId);
      case ProgramNotFound() ->
        "redirect:/admin/programs?error=That program does not exist";
    };
  }

}
