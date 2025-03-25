package com.example.abroad.service.page;

import com.example.abroad.model.Application;
import com.example.abroad.model.Program;
import com.example.abroad.model.Program.Question;
import com.example.abroad.model.User;
import com.example.abroad.model.Application.Status;
import com.example.abroad.service.ApplicationService;
import com.example.abroad.service.ProgramService;
import com.example.abroad.service.UserService;
import com.example.abroad.service.page.ApplyToProgramService.ApplyToProgram;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    if (userService.isAdmin(user)) {
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
    var questions = programService.getQuestions(program);

    return new GetApplyPageData.Success(program, user, questions, maxDayOfBirth);
  }

  public ApplyToProgram applyToProgram(
    Integer programId,
    HttpSession session, 
    String major, 
    Double gpa, 
    LocalDate dob,
    Map<String, String> allAnswers
  ) {
      Map<String, String> answers = allAnswers.entrySet().stream()
        .filter(e -> e.getKey().startsWith("answers[") && e.getKey().endsWith("]"))
        .collect(Collectors.toMap(
            e -> e.getKey().substring(8, e.getKey().length() - 1),
            Map.Entry::getValue
        ));

      if (major.isBlank() || answers.values().stream().anyMatch(String::isBlank)) {
          return new ApplyToProgram.InvalidSubmission();
      }
      
      var user = userService.findUserFromSession(session).orElse(null);
      if (user == null) {
          return new ApplyToProgram.UserNotFound();
      }
      
      var application = new Application(user.username(), programId, dob, gpa, major, Status.APPLIED);
      applicationService.save(application);
      
      answers.forEach((questionId, answer) -> {
          int qId = Integer.parseInt(questionId);
          applicationService.saveResponse(application, qId, answer);
      });
      
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
