package com.example.abroad.controller.student;

import com.example.abroad.service.LetterOfRecommendationService;
import com.example.abroad.service.LetterOfRecommendationService.DeleteRecommendationRequest;
import com.example.abroad.service.LetterOfRecommendationService.RequestRecommendation;
import com.example.abroad.view.Alerts;
import com.example.abroad.service.page.student.ApplyToProgramService;
import com.example.abroad.service.page.student.ApplyToProgramService.ApplyToProgram;
import com.example.abroad.service.page.student.ApplyToProgramService.GetApplyPageData;
import com.example.abroad.service.FormatService;
import com.example.abroad.service.UserService;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public record ApplyToProgramController(
  ApplyToProgramService service, FormatService formatter, UserService userService,
  LetterOfRecommendationService letterOfRecommendationService
  ) {

  @GetMapping("/programs/{programId}/apply")
  public String applyToProgram(@PathVariable Integer programId, HttpSession session,
    Model model, @RequestParam Optional<String> error, @RequestParam Optional<String> success,
    @RequestParam Optional<String> warning, @RequestParam Optional<String> info) {
    return switch (service.getPageData(programId, session)) {
      case GetApplyPageData.Success(var program, var user, var questions, var maxDayOfBirth, var letterRequests, var missingPreReqs) -> {
        model.addAllAttributes(Map.of(
          "program", program,
          "user", user,
          "alerts", new Alerts(error, success, warning, info),
          "questions", questions,
          "maxDayOfBirth", maxDayOfBirth,
          "formatter", formatter,
          "letterRequests", letterRequests,
          "missingPreReqs", missingPreReqs
        ));
        yield "student/apply-to-program :: page";
      }
      case GetApplyPageData.UserNotFound() ->
        "redirect:/login?error=You are not logged in";
      case GetApplyPageData.ProgramNotFound() ->
        "redirect:/programs?error=That program does not exist";
      case GetApplyPageData.StudentAlreadyApplied(Integer id, String username) -> String.format(
        "redirect:/applications/%s?error=You have already applied to this program",
        id);
      case GetApplyPageData.UserNotStudent() ->
        String.format("redirect:/admin/programs/%d?error=You are not a student", programId);
      case GetApplyPageData.ULinkNotSet() ->
        "redirect:/profile?error=You need to set your uLink before applying to a program#ulink";
    };
  }



  @PostMapping("/programs/{programId}/apply")
  public String applyToProgramPost(@PathVariable Integer programId, HttpSession session,
    @RequestParam String major, @RequestParam Double gpa, @RequestParam LocalDate dob,
    @RequestParam List<String> answers, @RequestParam List<Integer> questionIds
  ) {
    return switch (service.applyToProgram(programId, session, major, gpa, dob, answers, questionIds)) {
      case ApplyToProgram.Success(var p, var u) ->
        "redirect:/applications/" + programId + "?success=Application submitted";
      case ApplyToProgram.UserNotFound() -> "redirect:/login?error=You are not logged in";
      case ApplyToProgram.InvalidSubmission() -> "redirect:/programs/" + programId + "/apply?error=Invalid submission";
      case ApplyToProgram.ProgramNotFound() -> "redirect:/programs?error=That program does not exist";
    };
  }
  @PostMapping("/programs/{programId}/delete-letter-request")
  public String deleteRequest(@PathVariable Integer programId, HttpSession session, @RequestParam String email) {
    return switch (letterOfRecommendationService.deleteRecommendationRequest(programId, session, email)) {
      case DeleteRecommendationRequest.Success() ->
        "redirect:/programs/" + programId + "/apply?success=Request deleted#letter-requests";
      case DeleteRecommendationRequest.UserNotFound() -> "redirect:/login?error=You are not logged in";
   };
  }
  @PostMapping("/programs/{programId}/request-letter")
  public String requestRecommendation(@PathVariable Integer programId, HttpSession session,
    @RequestParam String email, @RequestParam String name, Model model) {
    return switch (letterOfRecommendationService.requestRecommendation(programId, session, email, name)) {
      case RequestRecommendation.ProgramNotFound() ->
        "redirect:/programs?error=That program does not exist";
      case RequestRecommendation.UserNotFound() -> "redirect:/login?error=You are not logged in";
      case RequestRecommendation.StudentAlreadyAsked() -> "redirect:/programs/" + programId
        + "/apply?error=You have already requested a recommendation#letter-requests";
      case RequestRecommendation.Success() -> {
        model.addAttribute("success", "Request sent");
        yield "redirect:/programs/" + programId + "/apply?success=Request sent#letter-requests";
      }
      case RequestRecommendation.EmailError() -> "redirect:/programs/" + programId + "/apply?error=Error sending email#letter-requests";
    };
  }
}
