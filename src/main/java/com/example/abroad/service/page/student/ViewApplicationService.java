package com.example.abroad.service.page.student;

import com.example.abroad.model.Application;
import com.example.abroad.model.Application.LetterOfRecommendation;
import com.example.abroad.model.Application.Response;
import com.example.abroad.model.Program;
import com.example.abroad.model.Program.PreReq;
import com.example.abroad.model.Program.Question;
import com.example.abroad.model.User;
import com.example.abroad.model.User.Course;
import com.example.abroad.service.ApplicationService;
import com.example.abroad.service.AuditService;
import com.example.abroad.service.ProgramService;
import com.example.abroad.service.ULinkService;
import com.example.abroad.service.UserService;
import com.example.abroad.service.page.student.ListApplicationsService.GetApplicationsResult;

import jakarta.servlet.http.HttpSession;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public record ViewApplicationService(
    ApplicationService applicationService,
    ProgramService programService,
    UserService userService,
    AuditService auditService,
    ULinkService uLinkService
) {

  public GetApplicationResult getApplication(Integer programId, HttpSession session) {
    var userOpt = userService.findUserFromSession(session);
    if (userOpt.isEmpty()) {
      return new GetApplicationResult.UserNotFound();
    }
    User user = userOpt.get();
    if (userService.isPartner(user)) {
      return new GetApplicationResult.UserNotFound();
    }
    var program = programService.findById(programId).orElse(null);
    if (program == null) {
      return new GetApplicationResult.ProgramNotFound();
    }
    Optional<Application> appOpt = applicationService.findByProgramAndStudent(program, user);
    if (appOpt.isEmpty()) {
      return new GetApplicationResult.ApplicationNotFound();
    }
    Application app = appOpt.get();

    if (!app.student().equals(user.username())) {
      return new GetApplicationResult.AccessDenied();
    }

    Optional<Program> progOpt = programService.findById(app.programId());
    if (progOpt.isEmpty()) {
      return new GetApplicationResult.ProgramNotFound();
    }
    Program prog = progOpt.get();

    boolean editable = false;
    LocalDate today = LocalDate.now();
    if (app.status() == Application.Status.APPLIED &&
      (today.isAfter(prog.applicationOpen()) || today.isEqual(prog.applicationOpen())) &&
      (today.isBefore(prog.applicationClose()) || today.isEqual(prog.applicationClose()))) {
      editable = true;
    }
    var responses = applicationService.getResponses(app);
    Map<Integer, Response> responseMap = responses.stream()
      .collect(Collectors.toMap(Response::question, Function.identity(), (r1, r2) -> r1));
    var questions = programService.getQuestions(prog);
    var letterRequests = letterOfRecInfo(user, program);

    var takenCourseCodes = userService.findCoursesByUsername(user.username())
      .stream()
      .filter(course -> course.grade().matches("^(?:[ABCD][+-]?|S|IP)$"))
      .map(Course::code)
      .toList();
    var preReqs = programService.getPreReqs(program)
      .stream()
      .map(p -> new PreReqInfo(p.courseCode(), takenCourseCodes.contains(p.courseCode())))
      .toList();

    return new GetApplicationResult.Success(app, prog, user, editable, responseMap, questions, letterRequests, preReqs);
  }

  public GetApplicationResult updateResponses(
      Integer programId,
      double gpa,
      String major,
      LocalDate dateOfBirth,
      @RequestParam Map<String, String> answers,
      HttpSession session) {
    var userOpt = userService.findUserFromSession(session);
    if (userOpt.isEmpty()) {
      return new GetApplicationResult.UserNotFound();
    }
    User user = userOpt.get();

    Map<String, String> ans = answers.entrySet().stream()
        .filter(e -> e.getKey().startsWith("answers[") && e.getKey().endsWith("]"))
        .collect(Collectors.toMap(
            e -> e.getKey().substring(8, e.getKey().length() - 1),
            Map.Entry::getValue
        ));

    GetApplicationResult result = getApplication(programId, session);
    if (!(result instanceof GetApplicationResult.Success success)) {
      return result;
    }

    if (!success.editable()) {
      return new GetApplicationResult.NotEditable();
    }
    
    var newApp = success.application()
      .withGpa(gpa)
      .withMajor(major)
      .withDateOfBirth(dateOfBirth);

    applicationService.save(newApp);
    
    ans.forEach((questionId, answer) -> {
        int qId = Integer.parseInt(questionId);
        applicationService.saveResponse(newApp, qId, answer);
    });

    var responses = applicationService.getResponses(newApp);
    Map<Integer, Response> responseMap = responses.stream()
      .collect(Collectors.toMap(Response::question, Function.identity(), (r1, r2) -> r1));
    Program program = programService.findById(newApp.programId()).orElse(null);
    var questions = programService.getQuestions(program);
    var letterRequests = letterOfRecInfo(user, program);
    var takenCourseCodes = userService.findCoursesByUsername(user.username())
      .stream()
      .filter(course -> course.grade().matches("^(?:[ABCD][+-]?|S|IP)$"))
      .map(Course::code)
      .toList();
    var preReqs = programService.getPreReqs(program)
      .stream()
      .map(p -> new PreReqInfo(p.courseCode(), takenCourseCodes.contains(p.courseCode())))
      .toList();

    auditService.logEvent("Application updated with new responses");

    return new GetApplicationResult.Success(newApp, success.program(), success.user(), true, responseMap, questions, letterRequests, preReqs);
  }

  public GetApplicationResult changeStatus(Integer programId, Application.Status newStatus, HttpSession session) {
    GetApplicationResult result = getApplication(programId, session);
    if (!(result instanceof GetApplicationResult.Success success)) {
      return result;
    }

    var userOpt = userService.findUserFromSession(session);
    if (userOpt.isEmpty()) {
      return new GetApplicationResult.UserNotFound();
    }
    User user = userOpt.get();

    Application app = success.application();

    if (newStatus == Application.Status.WITHDRAWN) {
      if (app.status() == Application.Status.CANCELLED || app.status() == Application.Status.WITHDRAWN) {
        return new GetApplicationResult.IllegalStatusChange();
      }
    } else if (newStatus == Application.Status.APPLIED) {
      if (app.status() != Application.Status.WITHDRAWN) {
        return new GetApplicationResult.IllegalStatusChange();
      }
    }

    applicationService.save(app.withStatus(newStatus));

    boolean editable = false;
    LocalDate today = LocalDate.now();
    if (app.status() == Application.Status.APPLIED &&
      (today.isAfter(success.program().applicationOpen()) || today.isEqual(success.program().applicationOpen())) &&
      (today.isBefore(success.program().applicationClose()) || today.isEqual(success.program().applicationClose()))) {
      editable = true;
    }
    
    var responses = applicationService.getResponses(app);
    Map<Integer, Response> responseMap = responses.stream()
      .collect(Collectors.toMap(Response::question, Function.identity(), (r1, r2) -> r1));
    Program program = programService.findById(app.programId()).orElse(null);
    var questions = programService.getQuestions(program);
    var letterRequests = letterOfRecInfo(user, program);
    var takenCourseCodes = userService.findCoursesByUsername(user.username())
      .stream()
      .filter(course -> course.grade().matches("^(?:[ABCD][+-]?|S|IP)$"))
      .map(Course::code)
      .toList();
    var preReqs = programService.getPreReqs(program)
      .stream()
      .map(p -> new PreReqInfo(p.courseCode(), takenCourseCodes.contains(p.courseCode())))
      .toList();

    auditService.logEvent("Application updated with new status: " + newStatus.name());

    return new GetApplicationResult.Success(app, success.program(), success.user(), editable, responseMap, questions, letterRequests, preReqs);
  }

  public record LetterOfRecInfo(String email, String name, Boolean submitted, Instant timestamp) { }

  private List<LetterOfRecInfo> letterOfRecInfo(User student, Program program) {
    return applicationService.getRecommendationRequests(program, student)
      .stream()
      .map(rec -> {
         Optional<LetterOfRecommendation> letterOpt = applicationService.findLetterOfRecommendation(
             rec.programId(), rec.student(), rec.email());
         Instant timestamp = letterOpt.map(LetterOfRecommendation::timestamp).orElse(null);
         boolean isSubmitted = letterOpt.isPresent();
         return new LetterOfRecInfo(rec.email(), rec.name(), isSubmitted, timestamp);
      })
      .collect(Collectors.toList());
  }

  public sealed interface RefreshULink {
    record Success() implements RefreshULink {
    }

    record UserNotFound() implements RefreshULink {
    }

    record NotLoggedIn() implements RefreshULink {
    }
    record ConnectionError() implements RefreshULink {
    }
    record UserLacksPermission() implements RefreshULink {
    }
  }


  public RefreshULink refreshULink(HttpSession session, Integer programId) {
    var user = userService.findUserFromSession(session).orElse(null);
    if (user == null) {
      return new RefreshULink.NotLoggedIn();
    }
    var application = applicationService.findByProgramIdAndStudentUsername(programId, user.username()).orElse(null);
    if (application == null) {
      return new RefreshULink.UserNotFound();
    }
    var program = programService.findById(programId).orElse(null);
    if (program == null) {
      return new RefreshULink.UserNotFound();
    }
    var student = userService.findByUsername(user.username()).orElse(null);
    if (student == null) {
      return new RefreshULink.UserNotFound();
    }

    return switch(uLinkService.refreshCourses(student)) {
      case ULinkService.RefreshCourses.Success() -> new RefreshULink.Success();
      case ULinkService.RefreshCourses.TranscriptServiceError() -> new RefreshULink.ConnectionError();
    };

  }

  public record PreReqInfo(String code, Boolean completed) {
  }

  public sealed interface GetApplicationResult {

    record Success(Application application, Program program, User user, boolean editable, Map<Integer, Response> responses, List<Question> questions, List<LetterOfRecInfo> letterRequests, List<PreReqInfo> preReqs)
        implements GetApplicationResult {
    }

    record UserNotFound() implements GetApplicationResult {
    }

    record ApplicationNotFound() implements GetApplicationResult {
    }

    record AccessDenied() implements GetApplicationResult {
    }

    record ProgramNotFound() implements GetApplicationResult {
    }

    record NotEditable() implements GetApplicationResult {
    }

    record IllegalStatusChange() implements GetApplicationResult {
    }
  }
}