package com.example.abroad.controller.admin;

import com.example.abroad.service.AdminProgramInfoService;
import com.example.abroad.service.AdminProgramInfoService.DeleteProgramOutput;
import com.example.abroad.service.AdminProgramInfoService.DeleteProgramOutput.ProgramNotFound;
import com.example.abroad.service.AdminProgramInfoService.GetProgramInfoOutput;
import com.example.abroad.service.FormatService;
import jakarta.servlet.http.HttpServletRequest;
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
      case GetProgramInfoOutput.UserNotFound() -> {
        return "redirect:/login?error=You are not logged in";
      }
      case GetProgramInfoOutput.UserNotAdmin() -> {
        return String.format("redirect:/programs/%s?error=You are not an admin", programId);
      }
      case GetProgramInfoOutput.ProgramNotFound(var user) -> {
        model.addAttribute("user", user);
        return "program-info :: not-found";
      }
      case GetProgramInfoOutput.Success(var program, var applicants, var user) -> {
        model.addAttribute("program", program);
        model.addAttribute("applicants", applicants);
        model.addAttribute("user", user);
        model.addAttribute("formatter", formatter);
        return "admin/program-info :: page";
      }
    }
  }

  @PostMapping("/admin/programs/{programId}/delete")
  public String deleteProgram(@PathVariable Integer programId, HttpServletRequest request) {
    return switch (service.deleteProgram(programId, request)) {
      case DeleteProgramOutput.Success() ->
        "redirect:/admin/programs?success=Program deleted";
      case DeleteProgramOutput.UserNotFound() ->
        "redirect:/login?error=You are not logged in";
      case DeleteProgramOutput.UserNotAdmin() ->
        String.format("redirect:/programs/%s?error=You are not an admin", programId);
      case ProgramNotFound() ->
        "redirect:/admin/programs?error=That program does not exist";
    };
  }

}
