package com.example.abroad.controller.student;

import com.example.abroad.controller.Alerts;
import com.example.abroad.service.ApplyToProgramService;
import com.example.abroad.service.ApplyToProgramService.ApplyToProgramOutput;
import com.example.abroad.service.ApplyToProgramService.GetPageDataOutput;
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
public record ApplyToProgramController(ApplyToProgramService service) {

  @GetMapping("/apply/{programId}")
  public String applyToProgram(@PathVariable Integer programId, HttpServletRequest request,
    Model model, @RequestParam Optional<String> error) {
    switch (service.getPageData(programId, request)) {
      case GetPageDataOutput.Success(var program, var user, var questions) -> {
        model.addAttribute("program", program);
        model.addAttribute("user", user);
        model.addAttribute("alerts",
          new Alerts(error, Optional.empty(), Optional.empty(), Optional.empty()));
        model.addAttribute("questions", questions);
        return "apply-to-program :: page";
      }
      case GetPageDataOutput.UserNotFound() -> {
        return "redirect:/login?error=You are not logged in";
      }
      case GetPageDataOutput.ProgramNotFound() -> {
        return "redirect:/programs?error=That program does not exist";
      }
      case GetPageDataOutput.StudentAlreadyApplied() -> {
        return String.format("redirect:/applications/%s?error=You have already applied to this program",
          programId);
      }
    }
  }

  @PostMapping("/apply/{programId}")
  public String applyToProgramPost(@PathVariable Integer programId, HttpServletRequest request,
    @RequestParam String major, @RequestParam Double gpa, @RequestParam LocalDate dob,
    @RequestParam String answer1, @RequestParam String answer2, @RequestParam String answer3,
    @RequestParam String answer4, @RequestParam String answer5
  ) {
    switch (service.applyToProgram(programId, request, major, gpa, dob, answer1, answer2, answer3,
      answer4, answer5)) {
      case ApplyToProgramOutput.Success() -> {
        return "redirect:/applications/" + programId;
      }
      case ApplyToProgramOutput.UserNotFound() -> {
        return "redirect:/login?error=You are not logged in";
      }
    }
  }

}
