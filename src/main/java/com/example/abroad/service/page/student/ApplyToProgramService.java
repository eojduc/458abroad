package com.example.abroad.service.page.student;

import com.example.abroad.model.Application;
import com.example.abroad.model.Application.PaymentStatus;
import com.example.abroad.model.Program;
import com.example.abroad.model.Program.Question;
import com.example.abroad.model.User;
import com.example.abroad.model.Application.Status;
import com.example.abroad.service.ApplicationService;
import com.example.abroad.service.AuditService;
import com.example.abroad.service.EmailService;
import com.example.abroad.service.ProgramService;
import com.example.abroad.service.UserService;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public record ApplyToProgramService(
  ApplicationService applicationService,
  ProgramService programService,
  UserService userService,
  EmailService emailService,
  AuditService auditService
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
    var questions = programService.getQuestions(program);
    var letterRequests = letterOfRecInfo(user, program);

    return new GetApplyPageData.Success(program, user, questions, maxDayOfBirth, letterRequests);
  }



  public ApplyToProgram applyToProgram(
    Integer programId,
    HttpSession session, String major, Double gpa, LocalDate dob,
    List<String> answers, List<Integer> questionIds
  ) {
    if (major.isBlank() || answers.stream().anyMatch(String::isEmpty)) {
      return new ApplyToProgram.InvalidSubmission();
    }
    var user = userService.findUserFromSession(session).orElse(null);
    if (user == null) {
      return new ApplyToProgram.UserNotFound();
    }
    var program = programService.findById(programId).orElse(null);
    if (program == null) {
      return new ApplyToProgram.ProgramNotFound();
    }
    var paymentStatus = program.trackPayment() ? PaymentStatus.UNPAID : PaymentStatus.FULLY_PAID;
    var application = new Application(user.username(),
      programId, dob, gpa, major, Status.APPLIED, paymentStatus
    );
    applicationService.save(application);
    for (int i = 0; i < answers.size(); i++) {
      applicationService.saveResponse(application, questionIds.get(i), answers.get(i));
    }
    auditService.logEvent("User " + user.username() + " successfully applied to program " + programId);
    return new ApplyToProgram.Success(application.programId(), application.student());
  }

  public sealed interface GetApplyPageData {
    record Success(Program program, User user, List<Question> questions,
                   String maxDateOfBirth, List<LetterOfRecInfo> letterRequests) implements GetApplyPageData { }
    record UserNotFound() implements GetApplyPageData { }
    record ProgramNotFound() implements GetApplyPageData { }
    record StudentAlreadyApplied(Integer programId, String student) implements GetApplyPageData { }
    record UserNotStudent() implements GetApplyPageData { }
  }

  public record LetterOfRecInfo(String email, String name, Boolean submitted) { }

  private List<LetterOfRecInfo> letterOfRecInfo(User student, Program program) {
    return applicationService.getRecommendationRequests(program, student)
      .stream()
      .map(rec -> new LetterOfRecInfo(rec.email(), rec.name(), isSubmitted(rec)))
      .toList();
  }
  private Boolean isSubmitted(Application.RecommendationRequest request) {
    return applicationService.findLetterOfRecommendation(request.programId(), request.student(),
      request.email()).isPresent();
  }

  public sealed interface ApplyToProgram {
    record Success(Integer programId, String username) implements ApplyToProgram { }
    record UserNotFound() implements ApplyToProgram { }
    record InvalidSubmission() implements ApplyToProgram { }
    record ProgramNotFound() implements ApplyToProgram { }
  }


}
