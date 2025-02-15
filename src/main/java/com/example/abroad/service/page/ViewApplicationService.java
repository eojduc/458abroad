package com.example.abroad.service.page;

import com.example.abroad.model.Application;
import com.example.abroad.model.Program;
import com.example.abroad.respository.ApplicationRepository;
import com.example.abroad.respository.ProgramRepository;
import com.example.abroad.model.User;

import com.example.abroad.service.UserService;
import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public record ViewApplicationService(
    ApplicationRepository applicationRepository,
    ProgramRepository programRepository,
    UserService userService) {

  public GetApplicationResult getApplication(String applicationId, HttpSession session) {
    var userOpt = userService.findUserFromSession(session);
    if (userOpt.isEmpty()) {
      return new GetApplicationResult.UserNotFound();
    }
    User user = userOpt.get();

    Optional<Application> appOpt = applicationRepository.findById(applicationId);
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

    return new GetApplicationResult.Success(app, prog, user, editable);
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

    Application app = success.application();

    app.setAnswer1(answer1);
    app.setAnswer2(answer2);
    app.setAnswer3(answer3);
    app.setAnswer4(answer4);
    app.setAnswer5(answer5);

    app.setGpa(gpa);
    app.setMajor(major);
    app.setBirthdate(dateOfBirth);

    applicationRepository.save(app);

    return new GetApplicationResult.Success(app, success.program(), success.user(), success.editable());
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

    app.setStatus(newStatus);

    applicationRepository.save(app);

    boolean editable = false;
    LocalDate today = LocalDate.now();
    if (app.status() == Application.Status.APPLIED &&
      today.isAfter(success.program().applicationOpen()) &&
      today.isBefore(success.program().applicationClose())) {
      editable = true;
    }

    return new GetApplicationResult.Success(app, success.program(), success.user(), editable);
  }

  public sealed interface GetApplicationResult {

    record Success(Application application, Program program, User user, boolean editable)
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