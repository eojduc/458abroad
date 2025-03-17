package com.example.abroad.service.page;

import com.example.abroad.model.Application;
import com.example.abroad.model.Application.Response;
import com.example.abroad.model.Application.Response.Question;
import com.example.abroad.model.Application.Status;
import com.example.abroad.model.Program;
import com.example.abroad.model.User;
import com.example.abroad.service.ApplicationService;
import com.example.abroad.service.ProgramService;
import com.example.abroad.service.UserService;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public record ApplyToProgramService(
  ApplicationService applicationService,
  ProgramService programService,
  UserService userService
) {


  public GetApplyPageData getPageData(Integer programId, HttpSession session) {
    var user = userService.findUserFromSession(session).orElse(null);
    if (user == null) {
      return new GetApplyPageData.UserNotFound();
    }
    if (!userService.isStudent(user)) {
      return new GetApplyPageData.UserNotStudent();
    }
    var program = programService.findById(programId).orElse(null);
    if (program == null) {
      return new GetApplyPageData.ProgramNotFound();
    }
    var existingApplication = applicationService.findByProgramAndStudent(program, user);
    if (existingApplication.isPresent()) {
      return new GetApplyPageData.StudentAlreadyApplied(existingApplication.get().programId(), user.username());
    }
    var maxDayOfBirth = LocalDate.now().minusYears(10).format(DateTimeFormatter.ISO_DATE);
    return new GetApplyPageData.Success(program, user, Arrays.asList(Question.values()), maxDayOfBirth);
  }

  public ApplyToProgram applyToProgram(
    Integer programId,
    HttpSession session, String major, Double gpa, LocalDate dob,
    String answer1, String answer2, String answer3, String answer4, String answer5
  ) {
    if (major.isBlank() || answer1.isBlank() || answer2.isBlank()
      || answer3.isBlank() || answer4.isBlank() || answer5.isBlank()) {
      return new ApplyToProgram.InvalidSubmission();
    }
    var user = userService.findUserFromSession(session).orElse(null);
    if (user == null) {
      return new ApplyToProgram.UserNotFound();
    }
    var application = new Application(user.username(),
      programId, dob, gpa, major, Status.APPLIED
    );
    applicationService.save(application);
    applicationService.saveResponse(application, Response.Question.WHY_THIS_PROGRAM, answer1);
    applicationService.saveResponse(application, Response.Question.ALIGN_WITH_CAREER, answer2);
    applicationService.saveResponse(application, Response.Question.ANTICIPATED_CHALLENGES, answer3);
    applicationService.saveResponse(application, Response.Question.ADAPTED_TO_ENVIRONMENT, answer4);
    applicationService.saveResponse(application, Response.Question.UNIQUE_PERSPECTIVE, answer5);

    return new ApplyToProgram.Success(application.programId(), application.student());
  }

  public sealed interface GetApplyPageData {
    record Success(Program program, User user, List<Question> questions,
                   String maxDateOfBirth) implements GetApplyPageData { }
    record UserNotFound() implements GetApplyPageData { }
    record ProgramNotFound() implements GetApplyPageData { }
    record StudentAlreadyApplied(Integer programId, String student) implements GetApplyPageData { }
    record UserNotStudent() implements GetApplyPageData { }
  }

  public sealed interface ApplyToProgram {
    record Success(Integer programId, String username) implements ApplyToProgram { }
    record UserNotFound() implements ApplyToProgram { }
    record InvalidSubmission() implements ApplyToProgram { }
  }


}
