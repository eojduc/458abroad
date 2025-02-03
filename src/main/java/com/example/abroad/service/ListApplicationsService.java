package com.example.abroad.service;

import com.example.abroad.model.Application;
import com.example.abroad.model.User;
import com.example.abroad.respository.ApplicationRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public record ListApplicationsService(ApplicationRepository applicationRepository, UserService userService) {

  public GetApplicationsResult getApplications(HttpSession session) {
    var user = userService.getUser(session).orElse(null);
    if (user == null) {
      return new GetApplicationsResult.UserNotFound();
    }
    List<Application> applications = applicationRepository
      .findProgramApplicationsByStudent(user.username())
      .stream()
      .filter(Optional::isPresent)
      .map(Optional::get)
      .collect(Collectors.toList());
    return new GetApplicationsResult.Success(applications, user);
  }

  public sealed interface GetApplicationsResult
    permits GetApplicationsResult.Success, GetApplicationsResult.UserNotFound {

    record Success(List<Application> applications, User user) implements GetApplicationsResult {}
    record UserNotFound() implements GetApplicationsResult {}
  }
}