package com.example.abroad.service;

import com.example.abroad.model.Admin;
import com.example.abroad.model.Application;
import com.example.abroad.model.Program;
import com.example.abroad.model.Student;
import com.example.abroad.respository.ApplicationRepository;
import com.example.abroad.respository.ProgramRepository;
import com.example.abroad.respository.StudentRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

@Service
public record AdminApplicationInfoService(
  FormatService formatService,
  UserService userService,
  ProgramRepository programRepository,
  ApplicationRepository applicationRepository,
  StudentRepository studentRepository) {


  public GetApplicationInfo getApplicationInfo(String applicationId, HttpServletRequest request) {
    var user = userService.getUser(request).orElse(null);
    if (user == null) {
      return new GetApplicationInfo.NotLoggedIn();
    }
    if (!(user instanceof Admin)) {
      return new GetApplicationInfo.UserNotAdmin();
    }
    var application = applicationRepository.findById(applicationId).orElse(null);
    if (application == null) {
      return new GetApplicationInfo.ApplicationNotFound();
    }
    var program = programRepository.findById(application.programId()).orElse(null);
    var student = studentRepository.findByUsername(application.student()).orElse(null);
    if (program == null || student == null) {
      return new GetApplicationInfo.ApplicationNotFound();
    }
    return new GetApplicationInfo.Success(program, student, application);
  }

  public UpdateApplicationStatus updateApplicationStatus(
    String applicationId, Application.Status status,
    HttpServletRequest request) {
    var user = userService.getUser(request).orElse(null);
    if (user == null) {
      return new UpdateApplicationStatus.NotLoggedIn();
    }
    if (!(user instanceof Admin)) {
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
    GetApplicationInfo.UserNotAdmin, GetApplicationInfo.NotLoggedIn, GetApplicationInfo.ApplicationNotFound {

    record Success(Program program, Student student, Application application) implements GetApplicationInfo {

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
