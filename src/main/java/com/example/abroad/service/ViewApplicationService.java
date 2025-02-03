package com.example.abroad.service;

import com.example.abroad.model.Application;
import com.example.abroad.respository.ApplicationRepository;
import com.example.abroad.model.User;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public record ViewApplicationService(ApplicationRepository applicationRepository, UserService userService) {

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
    return new GetApplicationResult.Success(app, user);
  }

  public sealed interface GetApplicationResult
    permits GetApplicationResult.Success, GetApplicationResult.UserNotFound,
            GetApplicationResult.ApplicationNotFound, GetApplicationResult.AccessDenied {

    record Success(Application application, User user) implements GetApplicationResult {}

    record UserNotFound() implements GetApplicationResult {}

    record ApplicationNotFound() implements GetApplicationResult {}

    record AccessDenied() implements GetApplicationResult {}
  }
}