package com.example.abroad.controller.student;

import com.example.abroad.model.Alerts;
import com.example.abroad.service.ApplyToProgramService;
import com.example.abroad.service.ApplyToProgramService.ApplyToProgram;
import com.example.abroad.service.ApplyToProgramService.GetApplyPageData;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public record ApplyToProgramController(ApplyToProgramService service) {

  @GetMapping("/apply/{programId}")
  public String applyToProgram(@PathVariable Integer programId, HttpServletRequest request,
    Model model, @RequestParam Optional<String> error) {
    switch (service.getPageData(programId, request)) {
      case GetApplyPageData.Success(var program, var user, var questions) -> {
        model.addAllAttributes(Map.of(
          "program", program,
          "user", user,
          "alerts", new Alerts(error, Optional.empty(), Optional.empty(), Optional.empty()),
          "questions", questions
        ));
        return "apply-to-program :: page";
      }
      case GetApplyPageData.UserNotFound() -> {
        return "redirect:/login?error=You are not logged in";
      }
      case GetApplyPageData.ProgramNotFound() -> {
        return "redirect:/programs?error=That program does not exist";
      }
      case GetApplyPageData.StudentAlreadyApplied(String applicationId) -> {
        return String.format("redirect:/applications/%s?error=You have already applied to this program",
          applicationId);
      }
    }
  }

  @PostMapping("/apply/{programId}")
  public String applyToProgramPost(@PathVariable Integer programId, HttpServletRequest request,
    @RequestParam String major, @RequestParam Double gpa, @RequestParam LocalDate dob,
    @RequestParam String answer1, @RequestParam String answer2, @RequestParam String answer3,
    @RequestParam String answer4, @RequestParam String answer5
  ) {
    return switch (service.applyToProgram(programId, request, major, gpa, dob, answer1, answer2, answer3,
      answer4, answer5)) {
      case ApplyToProgram.Success() -> "redirect:/applications/" + programId;
      case ApplyToProgram.UserNotFound() -> "redirect:/login?error=You are not logged in";
    };
  }

}
