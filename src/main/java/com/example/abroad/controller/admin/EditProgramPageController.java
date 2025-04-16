package com.example.abroad.controller.admin;

import com.example.abroad.model.Program.Partner;
import com.example.abroad.model.Program.Semester;
import com.example.abroad.service.FormatService;
import com.example.abroad.service.UserService;
import com.example.abroad.service.page.admin.EditProgramService;
import com.example.abroad.service.page.admin.EditProgramService.EditProgramPage;
import com.example.abroad.service.page.admin.EditProgramService.UpdateProgramInfo;
import com.example.abroad.view.Alerts;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public record EditProgramPageController(EditProgramService service, FormatService formatter,
                                        UserService userService) {

  private static final Logger logger = LoggerFactory.getLogger(EditProgramPageController.class);


  @GetMapping("/admin/programs/{programId}/edit")
  public String editProgramPage(HttpSession session, @PathVariable Integer programId,
      @RequestParam Optional<String> error, @RequestParam Optional<String> success,
      @RequestParam Optional<String> warning, @RequestParam Optional<String> info,
      Model model) {
    switch (service.getEditProgramInfo(programId, session)) {
      case EditProgramPage.Success(
          var program, var user, var facultyLeads, var selectedFaculty, var paymentPartners, var selectedPartners, var currentQuestions,
          var applicantsExist
      ) -> {
        List<String> selectedPartnerUsernames = selectedPartners.stream()
            .map(Partner::username)
            .toList();
        model.addAttribute("currentPrereqs", service.getCurrentPrereqs(program));
        model.addAllAttributes(Map.of(
            "program", program,
            "user", user,
            "alerts", new Alerts(error, success, warning, info),
            "applicantsExist", applicantsExist,
            "currentQuestions", currentQuestions,
            "formatter", formatter,
            "facultyLeads", facultyLeads,
            "selectedFacultyLeads",selectedFaculty,
            "partnerList", paymentPartners,
            "selectedPartnerUsernames", selectedPartnerUsernames
        ));
        return "admin/edit-program :: page";
      }
      case EditProgramPage.NotLoggedIn() -> {
        return "redirect:/login?error=You are not logged in";
      }
      case EditProgramPage.UserNotAdmin() -> {
        return "redirect:/programs?error=You are not an admin";
      }
      case EditProgramPage.ProgramNotFound() -> {
        return "redirect:/admin/programs?error=That program does not exist";
      }
    }
  }

  @PostMapping("/admin/programs/{programId}/edit")
  public String editProgramPost(@PathVariable Integer programId, @RequestParam String title,
      @RequestParam String description,
      @RequestParam Integer year, @RequestParam LocalDate startDate,
      @RequestParam LocalDate endDate,
      @RequestParam Semester semester, @RequestParam LocalDate applicationOpen,
      @RequestParam LocalDate documentDeadline,
      @RequestParam List<String> facultyLeads,
      @RequestParam(required = false) List<String> paymentPartners,
      @RequestParam(required = false) LocalDate paymentDate,
      @RequestParam LocalDate applicationClose,
      @RequestParam(required = false) List<String> selectedQuestions,
      @RequestParam(required = false) List<String> selectedPrereqs,
      @RequestParam(required = false) List<Integer> removedQuestions,
      HttpSession session) {
    return switch (service.updateProgramInfo(programId, title, description, year, startDate,
        endDate, paymentDate, semester, applicationOpen, applicationClose, facultyLeads, paymentPartners, documentDeadline,
        selectedQuestions, selectedPrereqs, removedQuestions, session)) {
      case UpdateProgramInfo.Success() ->
          String.format("redirect:/admin/programs/%d/edit?success=Program updated", programId);
      case UpdateProgramInfo.NotLoggedIn() -> "redirect:/login?error=You are not logged in";
      case UpdateProgramInfo.UserNotAdmin() -> "redirect:/?error=You are not an admin";
      case UpdateProgramInfo.ProgramNotFound() ->
          "redirect:/admin/programs?error=That program does not exist";
      case UpdateProgramInfo.InvalidProgramInfo(var m) ->
          String.format("redirect:/admin/programs/%d/edit?error=%s", programId, m);
      case UpdateProgramInfo.DatabaseError(var m) -> {
        logger.error("Database error", m);
        yield "redirect:/admin/programs?error=An unexpected error occurred";
      }
    };
  }

  @GetMapping("/admin/programs/{programId}/edit/has-applicants")
  @ResponseBody
  public Map<String, Boolean> checkProgramHasApplicants(@PathVariable Integer programId) {
    boolean hasApplicants = service.programHasApplicants(programId);
    return Map.of("hasApplicants", hasApplicants);
  }

}
