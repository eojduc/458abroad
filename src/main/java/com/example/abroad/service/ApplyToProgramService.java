package com.example.abroad.service;

import com.example.abroad.model.Application;
import com.example.abroad.model.Application.Status;
import com.example.abroad.model.Program;
import com.example.abroad.model.User;
import com.example.abroad.respository.ApplicationRepository;
import com.example.abroad.respository.ProgramRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;

@Service
public class ApplyToProgramService {

  private static final List<Question> QUESTIONS = List.of(
    new Question("answer1", "Why do you want to participate in this study abroad program?"),
    new Question("answer2", "How does this program align with your academic or career goals?"),
    new Question("answer3", "What challenges do you anticipate during this experience, and how will you address them?"),
    new Question("answer4", "Describe a time you adapted to a new or unfamiliar environment."),
    new Question("answer5", "What unique perspective or contribution will you bring to the group?")
  );

  private final ApplicationRepository applicationRepository;
  private final ProgramRepository programRepository;

  record Question(String field, String text) { }



  public ApplyToProgramService(ApplicationRepository applicationRepository, ProgramRepository programRepository) {
    this.applicationRepository = applicationRepository;
    this.programRepository = programRepository;
  }

  public sealed interface GetPageDataResponse permits PageData, UserNotFound, ProgramNotFound, StudentAlreadyApplied { }
  public record PageData(Program program, User user, List<Question> questions) implements GetPageDataResponse { }
  public record UserNotFound() implements GetPageDataResponse, ApplyToProgramResponse { }
  public record StudentAlreadyApplied() implements GetPageDataResponse { }
  public record ProgramNotFound() implements GetPageDataResponse { }

  public GetPageDataResponse getPageData(String programId, HttpServletRequest request) {
    var user = UserService.getUser(request).orElse(null);
    if (user == null) {
      return new UserNotFound();
    }
    var program = programRepository.findById(programId).orElse(null);
    if (program == null) {
      return new ProgramNotFound();
    }
    var alreadyApplied = applicationRepository.findByProgramIdAndStudent(programId, user.getUsername()).isPresent();
    if (alreadyApplied) {
      return new StudentAlreadyApplied();
    }
    return new PageData(program, user, QUESTIONS);
  }



  public sealed interface ApplyToProgramResponse permits SuccessfullyApplied, UserNotFound {
  }
  public record SuccessfullyApplied() implements ApplyToProgramResponse { }

  public ApplyToProgramResponse applyToProgram(
    String programId,
    HttpServletRequest request, String major, Float gpa, LocalDate dob,
    String answer1, String answer2, String answer3, String answer4, String answer5
  ) {
    var user = UserService.getUser(request).orElse(null);

    if (user == null) {
      return new UserNotFound();
    }
    var application = new Application(
      null, user.getUsername(), programId, dob, gpa, major, answer1, answer2, answer3, answer4, answer5, Status.APPLIED
    );
    applicationRepository.save(application);

    return new SuccessfullyApplied();
  }

}
