package com.example.abroad.service;

import com.example.abroad.model.Application;
import com.example.abroad.model.Application.Status;
import com.example.abroad.model.Program;
import com.example.abroad.model.User;
import com.example.abroad.respository.ApplicationRepository;
import com.example.abroad.respository.ProgramRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import org.springframework.stereotype.Service;

@Service
public class ApplyToProgramService {

  private final ApplicationRepository applicationRepository;
  private final ProgramRepository programRepository;
  private final UserService userService;

  public ApplyToProgramService(ApplicationRepository applicationRepository, UserService userService, ProgramRepository programRepository) {
    this.applicationRepository = applicationRepository;
    this.userService = userService;
    this.programRepository = programRepository;
  }

  public sealed interface GetPageDataResponse permits PageData, UserNotFound, ProgramNotFound { }
  public record PageData(Program program, User user) implements GetPageDataResponse { }
  public record UserNotFound() implements GetPageDataResponse { }
  public record ProgramNotFound() implements GetPageDataResponse { }

  public GetPageDataResponse getPageData(String programId, HttpServletRequest request) {
    var user = userService.getUser(request).orElse(null);
    if (user == null) {
      return new UserNotFound();
    }
    var program = programRepository.findById(programId).orElse(null);
    if (program == null) {
      return new ProgramNotFound();
    }
    return new PageData(program, user);
  }





  public sealed interface ApplyToProgramResponse permits SuccessfullyApplied, Failure {
  }
  public record SuccessfullyApplied() implements ApplyToProgramResponse {
  }
  public record Failure(String message) implements ApplyToProgramResponse {
  }

  public ApplyToProgramResponse applyToProgram(
    String programId,
    HttpServletRequest request, String major, Float gpa, LocalDate dob,
    String answer1, String answer2, String answer3, String answer4, String answer5
  ) {
    var user = userService.getUser(request).orElse(null);

    if (user == null) {
      return new Failure("User not found");
    }
    var application = new Application(
      null, user.getUsername(), programId, dob, gpa, major, answer1, answer2, answer3, answer4, answer5, Status.APPLIED
    );
    applicationRepository.save(application);

    return new SuccessfullyApplied();
  }

}
