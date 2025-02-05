package com.example.abroad.service;

import com.example.abroad.model.Application;
import com.example.abroad.model.Program;
import com.example.abroad.respository.ApplicationRepository;
import com.example.abroad.respository.ProgramRepository;
import com.example.abroad.model.User;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public record ViewApplicationService(
    ApplicationRepository applicationRepository,
    ProgramRepository programRepository,
    UserService userService) {

  public GetApplicationResult getApplication(String applicationId, HttpSession session) {
    var user = userService.getUser(session).orElse(null);
    if (user == null) {
      return new GetApplicationResult.UserNotFound();
    }
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
    return new GetApplicationResult.Success(app, prog, user);
  }

  public sealed interface GetApplicationResult
      permits GetApplicationResult.Success, GetApplicationResult.UserNotFound,
      GetApplicationResult.ApplicationNotFound, GetApplicationResult.AccessDenied,
      GetApplicationResult.ProgramNotFound {

    record Success(Application application, Program program, User user) implements GetApplicationResult {
    }

    record UserNotFound() implements GetApplicationResult {
    }

    record ApplicationNotFound() implements GetApplicationResult {
    }

    record AccessDenied() implements GetApplicationResult {
    }

    record ProgramNotFound() implements GetApplicationResult {
    }
  }
}