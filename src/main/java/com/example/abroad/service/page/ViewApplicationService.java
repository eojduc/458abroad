package com.example.abroad.service.page;

import com.example.abroad.model.Application;
import com.example.abroad.model.Application.Response;
import com.example.abroad.model.Program;
import com.example.abroad.respository.ApplicationRepository;
import com.example.abroad.respository.ProgramRepository;
import com.example.abroad.model.User;

import com.example.abroad.service.ApplicationService;
import com.example.abroad.service.UserService;
import jakarta.servlet.http.HttpSession;

import java.util.List;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public record ViewApplicationService(
    ApplicationService applicationService,
    ProgramRepository programRepository,
    UserService userService) {

  public GetApplicationResult getApplication(String applicationId, HttpSession session) {
    var userOpt = userService.findUserFromSession(session);
    if (userOpt.isEmpty()) {
      return new GetApplicationResult.UserNotFound();
    }
    User user = userOpt.get();

    Optional<Application> appOpt = applicationService.findById(applicationId);
    if (appOpt.isEmpty()) {
      return new GetApplicationResult.ApplicationNotFound();
    }
    Application app = appOpt.get();

    if (!app.student().equals(user.username())) {
      return new GetApplicationResult.AccessDenied();
    }

    Optional<Program> progOpt = programRepository.findById(app.programId());
    if (progOpt.isEmpty()) {
      return new GetApplicationResult.ProgramNotFound();
    }
    Program prog = progOpt.get();

    boolean editable = false;
    LocalDate today = LocalDate.now();
    if (app.status() == Application.Status.APPLIED &&
      today.isAfter(prog.applicationOpen()) &&
      today.isBefore(prog.applicationClose())) {
      editable = true;
    }
    var responses = applicationService.getResponses(app.id());

    return new GetApplicationResult.Success(app, prog, user, editable, responses);
  }

  public GetApplicationResult updateResponses(
      String applicationId,
      String answer1,
      String answer2,
      String answer3,
      String answer4,
      String answer5,
      double gpa,
      String major,
      LocalDate dateOfBirth,
      HttpSession session) {

    GetApplicationResult result = getApplication(applicationId, session);
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
    applicationService.saveResponse(newApp.id(), Application.Response.Question.WHY_THIS_PROGRAM, answer1);
    applicationService.saveResponse(newApp.id(), Application.Response.Question.ALIGN_WITH_CAREER, answer2);
    applicationService.saveResponse(newApp.id(), Application.Response.Question.ANTICIPATED_CHALLENGES, answer3);
    applicationService.saveResponse(newApp.id(), Application.Response.Question.ADAPTED_TO_ENVIRONMENT, answer4);
    applicationService.saveResponse(newApp.id(), Application.Response.Question.UNIQUE_PERSPECTIVE, answer5);
    var responses = applicationService.getResponses(newApp.id());
    return new GetApplicationResult.Success(newApp, success.program(), success.user(), true, responses);
  }

  public GetApplicationResult changeStatus(String applicationId, Application.Status newStatus, HttpSession session) {
    GetApplicationResult result = getApplication(applicationId, session);
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
      today.isAfter(success.program().applicationOpen()) &&
      today.isBefore(success.program().applicationClose())) {
      editable = true;
    }
    var responses = applicationService.getResponses(app.id());

    return new GetApplicationResult.Success(app, success.program(), success.user(), editable, responses);
  }

  public sealed interface GetApplicationResult {

    record Success(Application application, Program program, User user, boolean editable, List<Response> responses)
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