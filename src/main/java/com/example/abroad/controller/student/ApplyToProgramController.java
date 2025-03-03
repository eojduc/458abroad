package com.example.abroad.controller.student;

import com.example.abroad.model.Alerts;
import com.example.abroad.service.page.ApplyToProgramService;
import com.example.abroad.service.page.ApplyToProgramService.ApplyToProgram;
import com.example.abroad.service.page.ApplyToProgramService.GetApplyPageData;
import com.example.abroad.service.FormatService;
import com.example.abroad.service.UserService;
import jakarta.servlet.http.HttpSession;
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
public record ApplyToProgramController(ApplyToProgramService service, FormatService formatter, UserService userService) {

  @GetMapping("/programs/{programId}/apply")
  public String applyToProgram(@PathVariable Integer programId, HttpSession session,
    Model model, @RequestParam Optional<String> error, @RequestParam Optional<String> success,
    @RequestParam Optional<String> warning, @RequestParam Optional<String> info) {
    return switch (service.getPageData(programId, session)) {
      case GetApplyPageData.Success(var program, var user, var questions, var maxDayOfBirth) -> {
        model.addAllAttributes(Map.of(
          "program", program,
          "user", user,
          "alerts", new Alerts(error, success, warning, info),
          "questions", questions,
          "maxDayOfBirth", maxDayOfBirth,
          "formatter", formatter
        ));
        yield "student/apply-to-program :: page";
      }
      case GetApplyPageData.UserNotFound() ->
        "redirect:/login?error=You are not logged in";
      case GetApplyPageData.ProgramNotFound() ->
        "redirect:/programs?error=That program does not exist";
      case GetApplyPageData.StudentAlreadyApplied(String applicationId) -> String.format(
        "redirect:/applications/%s?error=You have already applied to this program",
        applicationId);
      case GetApplyPageData.UserNotStudent() ->
        String.format("redirect:/admin/programs/%d?error=You are not a student", programId);
    };
  }

  @PostMapping("/programs/{programId}/apply")
  public String applyToProgramPost(@PathVariable Integer programId, HttpSession session,
    @RequestParam String major, @RequestParam Double gpa, @RequestParam LocalDate dob,
    @RequestParam String answer1, @RequestParam String answer2, @RequestParam String answer3,
    @RequestParam String answer4, @RequestParam String answer5
  ) {
    return switch (service.applyToProgram(programId, session, major, gpa, dob, answer1, answer2,
      answer3, answer4, answer5)) {
      case ApplyToProgram.Success(var id) -> "redirect:/applications/" + id;
      case ApplyToProgram.UserNotFound() -> "redirect:/login?error=You are not logged in";
      case ApplyToProgram.InvalidSubmission() -> "redirect:/programs/" + programId + "/apply?error=Invalid submission";
    };
  }

}
