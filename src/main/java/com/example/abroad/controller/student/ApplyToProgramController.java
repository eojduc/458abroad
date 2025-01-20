package com.example.abroad.controller.student;

import com.example.abroad.service.ApplyToProgramService;
import com.example.abroad.service.ApplyToProgramService.ProgramNotFound;
import com.example.abroad.service.ApplyToProgramService.PageData;
import com.example.abroad.service.ApplyToProgramService.StudentAlreadyApplied;
import com.example.abroad.service.ApplyToProgramService.SuccessfullyApplied;
import com.example.abroad.service.ApplyToProgramService.UserNotFound;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ApplyToProgramController {

  private final ApplyToProgramService service;

  public ApplyToProgramController(ApplyToProgramService service) {
    this.service = service;
  }

  @GetMapping("/apply/{programId}")
  public String applyToProgram(@PathVariable String programId, HttpServletRequest request, Model model, @RequestParam Optional<String> error) {
    switch (service.getPageData(programId, request)) {
      case PageData(var program, var user, var questions) -> {
        model.addAttribute("program", program);
        model.addAttribute("user", user);
        model.addAttribute("error", error);
        model.addAttribute("questions", questions);
        return "apply-to-program :: page";
      }
      case UserNotFound() -> {
        return "redirect:/login";
      }
      case ProgramNotFound() -> {
        return "redirect:/programs";
      }
      case StudentAlreadyApplied() -> {
        return "redirect:/applications/" + programId + "/error=You have already applied to this program";
      }
    }
  }

  @PostMapping("/apply/{programId}")
  public String applyToProgramPost(@PathVariable String programId, HttpServletRequest request,
    @RequestParam String major, @RequestParam Double gpa, @RequestParam LocalDate dob,
    @RequestParam String answer1, @RequestParam String answer2, @RequestParam String answer3,
    @RequestParam String answer4, @RequestParam String answer5
  ) {
    switch (service.applyToProgram(programId, request, major, gpa, dob, answer1, answer2, answer3, answer4, answer5)) {
      case SuccessfullyApplied() -> {
        return "redirect:/applications";
      }
      case UserNotFound() -> {
        return "redirect:/login";
      }
    }
  }

}
