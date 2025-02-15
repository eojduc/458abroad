package com.example.abroad.service.admin;

import com.example.abroad.model.Application;
import com.example.abroad.model.Program;
import com.example.abroad.model.User;
import com.example.abroad.respository.ApplicationRepository;
import com.example.abroad.respository.ProgramRepository;
import com.example.abroad.service.FormatService;
import com.example.abroad.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

@Service
public record AdminApplicationInfoService(
  FormatService formatService,
  UserService userService,
  ProgramRepository programRepository,
  ApplicationRepository applicationRepository) {


  public GetApplicationInfo getApplicationInfo(String applicationId, HttpSession session) {
    var user = userService.findUserFromSession(session).orElse(null);
    if (user == null) {
      return new GetApplicationInfo.NotLoggedIn();
    }
    if (user.role() != User.Role.ADMIN) {
      return new GetApplicationInfo.UserNotAdmin();
    }
    var application = applicationRepository.findById(applicationId).orElse(null);
    if (application == null) {
      return new GetApplicationInfo.ApplicationNotFound();
    }
    var program = programRepository.findById(application.programId()).orElse(null);
    var student = userService.findByUsername(application.student()).orElse(null);
    if (program == null || student == null) {
      return new GetApplicationInfo.ApplicationNotFound();
    }
    return new GetApplicationInfo.Success(program, student, application, user);
  }

  public UpdateApplicationStatus updateApplicationStatus(
    String applicationId, Application.Status status,
    HttpSession session) {
    var user = userService.findUserFromSession(session).orElse(null);
    if (user == null) {
      return new UpdateApplicationStatus.NotLoggedIn();
    }
    if (user.role() != User.Role.ADMIN) {
      return new UpdateApplicationStatus.UserNotAdmin();
    }
    var application = applicationRepository.findById(applicationId).orElse(null);
    if (application == null) {
      return new UpdateApplicationStatus.ApplicationNotFound();
    }
    applicationRepository.updateStatus(applicationId, status);
    return new UpdateApplicationStatus.Success(status);
  }

  public sealed interface GetApplicationInfo permits GetApplicationInfo.Success,
    GetApplicationInfo.UserNotAdmin, GetApplicationInfo.NotLoggedIn,
    GetApplicationInfo.ApplicationNotFound {

    record Success(Program program, User student, Application application, User user) implements
      GetApplicationInfo {

    }

    record UserNotAdmin() implements GetApplicationInfo {

    }

    record NotLoggedIn() implements GetApplicationInfo {

    }

    record ApplicationNotFound() implements GetApplicationInfo {

    }

  }

  public sealed interface UpdateApplicationStatus permits UpdateApplicationStatus.Success,
    UpdateApplicationStatus.ApplicationNotFound, UpdateApplicationStatus.UserNotAdmin,
    UpdateApplicationStatus.NotLoggedIn {

    record Success(Application.Status status) implements UpdateApplicationStatus {

    }

    record ApplicationNotFound() implements UpdateApplicationStatus {

    }

    record UserNotAdmin() implements UpdateApplicationStatus {

    }

    record NotLoggedIn() implements UpdateApplicationStatus {

    }

  }
}
