package com.example.abroad.service.page.student;

import com.example.abroad.model.Application;
import com.example.abroad.model.Application.Response;
import com.example.abroad.model.Program;
import com.example.abroad.model.Program.Question;
import com.example.abroad.model.User;

import com.example.abroad.service.ApplicationService;
import com.example.abroad.service.AuditService;
import com.example.abroad.service.ProgramService;
import com.example.abroad.service.UserService;
import jakarta.servlet.http.HttpSession;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public record ViewApplicationService(
    ApplicationService applicationService,
    ProgramService programService,
    UserService userService,
    AuditService auditService
) {

  public GetApplicationResult getApplication(Integer programId, HttpSession session) {
    var userOpt = userService.findUserFromSession(session);
    if (userOpt.isEmpty()) {
      return new GetApplicationResult.UserNotFound();
    }
    User user = userOpt.get();
    var program = programService.findById(programId).orElse(null);
    if (program == null) {
      return new GetApplicationResult.ProgramNotFound();
    }
    Optional<Application> appOpt = applicationService.findByProgramAndStudent(program, user);
    if (appOpt.isEmpty()) {
      return new GetApplicationResult.ApplicationNotFound();
    }
    Application app = appOpt.get();

    if (!app.student().equals(user.username())) {
      return new GetApplicationResult.AccessDenied();
    }

    Optional<Program> progOpt = programService.findById(app.programId());
    if (progOpt.isEmpty()) {
      return new GetApplicationResult.ProgramNotFound();
    }
    Program prog = progOpt.get();

    boolean editable = false;
    LocalDate today = LocalDate.now();
    if (app.status() == Application.Status.APPLIED &&
      (today.isAfter(prog.applicationOpen()) || today.isEqual(prog.applicationOpen())) &&
      (today.isBefore(prog.applicationClose()) || today.isEqual(prog.applicationClose()))) {
      editable = true;
    }
    var responses = applicationService.getResponses(app);
    var questions = programService.getQuestions(prog);

    return new GetApplicationResult.Success(app, prog, user, editable, responses, questions);
  }

  public GetApplicationResult updateResponses(
      Integer programId,
      double gpa,
      String major,
      LocalDate dateOfBirth,
      @RequestParam Map<String, String> answers,
      HttpSession session) {

    Map<String, String> ans = answers.entrySet().stream()
        .filter(e -> e.getKey().startsWith("answers[") && e.getKey().endsWith("]"))
        .collect(Collectors.toMap(
            e -> e.getKey().substring(8, e.getKey().length() - 1),
            Map.Entry::getValue
        ));

    GetApplicationResult result = getApplication(programId, session);
    if (!(result instanceof GetApplicationResult.Success success)) {
      return result;
    }

    if (!success.editable()) {
      return new GetApplicationResult.NotEditable();
    }
    
    var newApp = success.application()
      .withGpa(gpa)
      .withMajor(major)
      .withDateOfBirth(dateOfBirth);

    applicationService.save(newApp);
    
    ans.forEach((questionId, answer) -> {
        int qId = Integer.parseInt(questionId);
        applicationService.saveResponse(newApp, qId, answer);
    });

    var responses = applicationService.getResponses(newApp);
    Program program = programService.findById(newApp.programId()).orElse(null);
    var questions = programService.getQuestions(program);

    auditService.logEvent("Application updated with new responses");

    return new GetApplicationResult.Success(newApp, success.program(), success.user(), true, responses, questions);
  }

  public GetApplicationResult changeStatus(Integer programId, Application.Status newStatus, HttpSession session) {
    GetApplicationResult result = getApplication(programId, session);
    if (!(result instanceof GetApplicationResult.Success success)) {
      return result;
    }

    Application app = success.application();

    if (newStatus == Application.Status.WITHDRAWN) {
      if (app.status() == Application.Status.CANCELLED || app.status() == Application.Status.WITHDRAWN) {
        return new GetApplicationResult.IllegalStatusChange();
      }
    } else if (newStatus == Application.Status.APPLIED) {
      if (app.status() != Application.Status.WITHDRAWN) {
        return new GetApplicationResult.IllegalStatusChange();
      }
    }

    applicationService.save(app.withStatus(newStatus));

    boolean editable = false;
    LocalDate today = LocalDate.now();
    if (app.status() == Application.Status.APPLIED &&
      (today.isAfter(success.program().applicationOpen()) || today.isEqual(success.program().applicationOpen())) &&
      (today.isBefore(success.program().applicationClose()) || today.isEqual(success.program().applicationClose()))) {
      editable = true;
    }
    
    var responses = applicationService.getResponses(app);
    Program program = programService.findById(app.programId()).orElse(null);
    var questions = programService.getQuestions(program);

    auditService.logEvent("Application updated with new status: " + newStatus.name());

    return new GetApplicationResult.Success(app, success.program(), success.user(), editable, responses, questions);
  }

  public sealed interface GetApplicationResult {

    record Success(Application application, Program program, User user, boolean editable, List<Response> responses, List<Question> questions)
        implements GetApplicationResult {
    }

    record UserNotFound() implements GetApplicationResult {
    }

    record ApplicationNotFound() implements GetApplicationResult {
    }

    record AccessDenied() implements GetApplicationResult {
    }

    record ProgramNotFound() implements GetApplicationResult {
    }

    record NotEditable() implements GetApplicationResult {
    }

    record IllegalStatusChange() implements GetApplicationResult {
    }
  }
}