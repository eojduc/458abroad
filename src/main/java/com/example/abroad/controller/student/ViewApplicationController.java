package com.example.abroad.controller.student;

import com.example.abroad.view.Alerts;
import com.example.abroad.model.Application;
import com.example.abroad.model.Program;
import com.example.abroad.respository.FacultyLeadRepository;
import com.example.abroad.service.DocumentService;
import com.example.abroad.service.FormatService;
import com.example.abroad.service.UserService;
import com.example.abroad.service.page.ViewApplicationService;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
public record ViewApplicationController(
        ViewApplicationService applicationService,
        FormatService formatter,
        UserService userService,
        DocumentService documentService,
        FacultyLeadRepository facultyLeadRepository) {

  @GetMapping("/applications/{programId}")
  public String viewApplication(
          @PathVariable Integer programId,
          HttpSession session,
          Model model,
          @RequestParam Optional<String> error,
          @RequestParam Optional<String> info,
          @RequestParam Optional<String> success,
          @RequestParam Optional<String> warning) {

    var result = applicationService.getApplication(programId, session);

    return switch (result) {
      case ViewApplicationService.GetApplicationResult.Success successRes -> {
        // Get document information for the application
        var documentStatuses = documentService.getDocumentStatuses(session, successRes.program().id());
        var missingCount = documentService.getMissingDocumentsCount(session, successRes.program().id());

        // Get faculty leads for the program
        List<Program.FacultyLead> facultyLeads = facultyLeadRepository.findById_ProgramId(successRes.program().id());

        // Create a map for the application-document pair similar to what's in ListApplicationsService
        var pair = new HashMap<String, Object>();
        pair.put("app", successRes.application());
        pair.put("prog", successRes.program());
        pair.put("documents", documentStatuses);
        pair.put("missingDocumentsCount", missingCount);
        pair.put("facultyLeads", facultyLeads);

        var allAttributes = new HashMap<String, Object>();
        allAttributes.put("app", successRes.application());
        allAttributes.put("prog", successRes.program());
        allAttributes.put("user", successRes.user());
        allAttributes.put("editable", successRes.editable());
        allAttributes.put("responses", successRes.responses());
        allAttributes.put("questions", successRes.questions());
        allAttributes.put("formatter", formatter);
        allAttributes.put("alerts", new Alerts(error, success, warning, info));
        allAttributes.put("pair", pair);
        allAttributes.put("facultyLeads", facultyLeads);

        model.addAllAttributes(allAttributes);
        yield "student/view-application :: page";
      }
      case ViewApplicationService.GetApplicationResult.UserNotFound() ->
              "redirect:/login?error=Not logged in";
      case ViewApplicationService.GetApplicationResult.ApplicationNotFound() ->
              "redirect:/?error=Application not found";
      case ViewApplicationService.GetApplicationResult.AccessDenied() ->
              "redirect:/?error=Access denied";
      case ViewApplicationService.GetApplicationResult.ProgramNotFound() ->
              "redirect:/dashboard?error=Program not found";
      case ViewApplicationService.GetApplicationResult.NotEditable ne ->
              "redirect:/dashboard?error=Application Not Editable";
      case ViewApplicationService.GetApplicationResult.IllegalStatusChange isc ->
              "redirect:/dashboard?error=Illegal Status Change";
    };
  }

  @PostMapping("/applications/{programId}/update")
  public String updateApplication(
          @PathVariable("programId") Integer programId,
          @RequestParam("answer1") String answer1,
          @RequestParam("answer2") String answer2,
          @RequestParam("answer3") String answer3,
          @RequestParam("answer4") String answer4,
          @RequestParam("answer5") String answer5,
          @RequestParam("gpa") double gpa,
          @RequestParam("major") String major,
          @RequestParam("dateOfBirth") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateOfBirth,
          HttpSession session,
          Model model,
          HttpServletResponse response) {

    var result = applicationService.updateResponses(programId, answer1, answer2, answer3, answer4, answer5, gpa, major,
            dateOfBirth, session);

    return switch (result) {
      case ViewApplicationService.GetApplicationResult.Success success -> {
        response.setHeader("HX-Redirect", "/applications/" + programId + "?success=Your responses have been saved!");
        yield null;
      }
      case ViewApplicationService.GetApplicationResult.UserNotFound() ->
              "redirect:/login?error=Not logged in";
      case ViewApplicationService.GetApplicationResult.ApplicationNotFound() ->
              "redirect:/dashboard?error=Application not found";
      case ViewApplicationService.GetApplicationResult.AccessDenied() ->
              "redirect:/dashboard?error=Access denied";
      case ViewApplicationService.GetApplicationResult.ProgramNotFound() ->
              "redirect:/dashboard?error=Program not found";
      case ViewApplicationService.GetApplicationResult.NotEditable ne ->
              "redirect:/dashboard?error=Application Not Editable";
      case ViewApplicationService.GetApplicationResult.IllegalStatusChange isc ->
              "redirect:/dashboard?error=Illegal Status Change";
    };
  }

  @PostMapping("/applications/{programId}/withdraw")
  public String withdrawApplication(@PathVariable("programId") Integer programId,
                                    HttpSession session,
                                    Model model) {

    var result = applicationService.changeStatus(programId, Application.Status.WITHDRAWN, session);

    return switch (result) {
      case ViewApplicationService.GetApplicationResult.Success success -> {
        // Get document information for the application
        var documentStatuses = documentService.getDocumentStatuses(session, success.program().id());
        var missingCount = documentService.getMissingDocumentsCount(session, success.program().id());

        // Get faculty leads for the program
        List<Program.FacultyLead> facultyLeads = facultyLeadRepository.findById_ProgramId(success.program().id());

        // Create a map for the application-document pair
        var pair = new HashMap<String, Object>();
        pair.put("app", success.application());
        pair.put("prog", success.program());
        pair.put("documents", documentStatuses);
        pair.put("missingDocumentsCount", missingCount);
        pair.put("facultyLeads", facultyLeads);

        model.addAllAttributes(Map.of(
                "app", success.application(),
                "prog", success.program(),
                "user", success.user(),
                "editable", success.editable(),
                "formatter", formatter,
                "pair", pair,
                "facultyLeads", facultyLeads,
                "responses", success.responses(),
                "questions", success.questions()));
        yield "student/view-application :: applicationContent";
      }
      case ViewApplicationService.GetApplicationResult.UserNotFound() ->
              "redirect:/login?error=Not logged in";
      case ViewApplicationService.GetApplicationResult.ApplicationNotFound() ->
              "redirect:/dashboard?error=Application not found";
      case ViewApplicationService.GetApplicationResult.AccessDenied() ->
              "redirect:/dashboard?error=Access denied";
      case ViewApplicationService.GetApplicationResult.ProgramNotFound() ->
              "redirect:/dashboard?error=Program not found";
      case ViewApplicationService.GetApplicationResult.NotEditable ne ->
              "redirect:/dashboard?error=Application Not Editable";
      case ViewApplicationService.GetApplicationResult.IllegalStatusChange isc ->
              "redirect:/dashboard?error=Illegal Status Change";
    };
  }

  @PostMapping("/applications/{programId}/reactivate")
  public String reactivateApplication(@PathVariable("programId") Integer programId,
                                      HttpSession session,
                                      Model model) {

    var result = applicationService.changeStatus(programId, Application.Status.APPLIED, session);

    return switch (result) {
      case ViewApplicationService.GetApplicationResult.Success success -> {
        // Get document information for the application
        var documentStatuses = documentService.getDocumentStatuses(session, success.program().id());
        var missingCount = documentService.getMissingDocumentsCount(session, programId);

        // Get faculty leads for the program
        List<Program.FacultyLead> facultyLeads = facultyLeadRepository.findById_ProgramId(success.program().id());

        // Create a map for the application-document pair
        var pair = new HashMap<String, Object>();
        pair.put("app", success.application());
        pair.put("prog", success.program());
        pair.put("documents", documentStatuses);
        pair.put("missingDocumentsCount", missingCount);
        pair.put("facultyLeads", facultyLeads);

        model.addAllAttributes(Map.of(
                "app", success.application(),
                "prog", success.program(),
                "user", success.user(),
                "editable", success.editable(),
                "responses", success.responses(),
                "questions", success.questions(),
                "pair", pair,
                "facultyLeads", facultyLeads,
                "formatter", formatter));
        yield "student/view-application :: applicationContent";
      }
      case ViewApplicationService.GetApplicationResult.UserNotFound() ->
              "redirect:/login?error=Not logged in";
      case ViewApplicationService.GetApplicationResult.ApplicationNotFound() ->
              "redirect:/dashboard?error=Application not found";
      case ViewApplicationService.GetApplicationResult.AccessDenied() ->
              "redirect:/dashboard?error=Access denied";
      case ViewApplicationService.GetApplicationResult.ProgramNotFound() ->
              "redirect:/dashboard?error=Program not found";
      case ViewApplicationService.GetApplicationResult.NotEditable ne ->
              "redirect:/dashboard?error=Application Not Editable";
      case ViewApplicationService.GetApplicationResult.IllegalStatusChange isc ->
              "redirect:/dashboard?error=Illegal Status Change";
    };
  }
}