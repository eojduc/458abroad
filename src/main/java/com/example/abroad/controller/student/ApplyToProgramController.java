package com.example.abroad.controller.student;

import com.example.abroad.model.Program;
import com.example.abroad.model.User;
import com.example.abroad.service.ApplyToProgramService;
import com.example.abroad.service.ApplyToProgramService.Failure;
import com.example.abroad.service.ApplyToProgramService.ProgramNotFound;
import com.example.abroad.service.ApplyToProgramService.PageData;
import com.example.abroad.service.ApplyToProgramService.SuccessfullyApplied;
import com.example.abroad.service.ApplyToProgramService.UserNotFound;
import com.example.abroad.service.ProgramInfoService.ProgramInfo;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
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
  public String applyToProgram(@PathVariable String programId, HttpServletRequest request, Model model) {
    switch (service.getPageData(programId, request)) {
      case PageData(Program program, User user) -> {
        model.addAttribute("program", program);
        model.addAttribute("user", user);
        return "apply";
      }
      case UserNotFound n -> {
        return "redirect:/login";
      }
      case ProgramNotFound n -> {
        return "redirect:/programs";
      }
    }
  }

  @PostMapping("/apply/{programId}")
  public String applyToProgramPost(@PathVariable String programId, HttpServletRequest request, Model model,
    @RequestParam String major, @RequestParam Float gpa, @RequestParam LocalDate dob,
    @RequestParam String answer1, @RequestParam String answer2, @RequestParam String answer3,
    @RequestParam String answer4, @RequestParam String answer5
  ) {
    switch (service.applyToProgram(programId, request, major, gpa, dob, answer1, answer2, answer3, answer4, answer5)) {
      case SuccessfullyApplied s -> {
        return "redirect:/programs";
      }
      case Failure(var message) -> {
        model.addAttribute("error", message);
        return "apply";
      }
    }
  }

}
