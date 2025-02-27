package com.example.abroad.service.page.admin;

import com.example.abroad.model.Application;
import com.example.abroad.model.Application.Note;
import com.example.abroad.model.Application.Status;
import com.example.abroad.model.Program;
import com.example.abroad.model.User;
import com.example.abroad.service.ApplicationService;
import com.example.abroad.service.ApplicationService.Documents;
import com.example.abroad.service.FormatService;
import com.example.abroad.service.ProgramService;
import com.example.abroad.service.UserService;
import jakarta.servlet.http.HttpSession;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public record AdminApplicationInfoService(
  FormatService formatService,
  UserService userService,
  ProgramService programService,
  ApplicationService applicationService
) {


  public GetApplicationInfo getApplicationInfo(String applicationId, HttpSession session) {
    var user = userService.findUserFromSession(session).orElse(null);
    if (user == null) {
      return new GetApplicationInfo.NotLoggedIn();
    }
    if (!user.isAdmin()) {
      return new GetApplicationInfo.UserNotAdmin();
    }
    var application = applicationService.findById(applicationId).orElse(null);
    if (application == null) {
      return new GetApplicationInfo.ApplicationNotFound();
    }
    var program = programService.findById(application.programId()).orElse(null);
    var student = userService.findByUsername(application.student()).orElse(null);
    var notes = applicationService.getNotes(application)
      .stream()
      .sorted(Comparator.comparing(Note::timestamp).reversed())
      .toList();
    var responses = applicationService.getResponses(application);
    var documents = applicationService.getLatestDocuments(application);
    if (program == null || student == null) {
      return new GetApplicationInfo.ApplicationNotFound();
    }
    var facultyLeads = programService.findFacultyLeads(program);
    var programIsPast = program.endDate().isBefore(LocalDate.now());
    var status = programIsPast && application.status() == Status.ENROLLED ? "COMPLETED" : application.status().toString();
    return new GetApplicationInfo.Success(
      program, student, application, user, notes, documents, status, facultyLeads, responses
    );
  }



  public UpdateApplicationStatus updateApplicationStatus(
    String applicationId, Application.Status status, HttpSession session
  ) {
    var user = userService.findUserFromSession(session).orElse(null);
    if (user == null) {
      return new UpdateApplicationStatus.NotLoggedIn();
    }
    if (!user.isAdmin()) {
      return new UpdateApplicationStatus.UserNotAdmin();
    }
    var application = applicationService.findById(applicationId).orElse(null);
    if (application == null) {
      return new UpdateApplicationStatus.ApplicationNotFound();
    }
    applicationService.updateStatus(application, status);
    var program = programService.findById(application.programId()).orElse(null);
    if (program == null) {
      return new UpdateApplicationStatus.ProgramNotFound();
    }
    var programIsPast = program.endDate().isBefore(LocalDate.now());
    var displayedStatus = programIsPast && status == Status.ENROLLED ? "COMPLETED" : status.toString();
    return new UpdateApplicationStatus.Success(displayedStatus);
  }

  public PostNote postNote(String applicationId, String note, HttpSession session) {
    var user = userService.findUserFromSession(session).orElse(null);
    if (user == null) {
      return new PostNote.NotLoggedIn();
    }
    if (!user.isAdmin()) {
      return new PostNote.UserNotAdmin();
    }
    var application = applicationService.findById(applicationId).orElse(null);
    if (application == null) {
      return new PostNote.ApplicationNotFound();
    }
    applicationService.saveNote(new Note(
      applicationId,
      user.username(),
      note,
      Instant.now()
    ));
    var notes = applicationService.getNotes(application)
      .stream()
      .sorted(Comparator.comparing(Note::timestamp))
      .toList()
      .reversed();
    return new PostNote.Success(notes);
  }
  public sealed interface PostNote {
    record Success(List<Application.Note> notes) implements PostNote {
    }
    record ApplicationNotFound() implements PostNote {
    }

    record UserNotAdmin() implements PostNote {
    }

    record NotLoggedIn() implements PostNote {
    }
  }

  public sealed interface GetApplicationInfo  {

    record Success(Program program, User student, Application application, User user, List<Note> notes,
                   Documents documents, String status, List<? extends User> facultyLeads,
                    List<Application.Response> responses
    ) implements
      GetApplicationInfo {

    }

    record UserNotAdmin() implements GetApplicationInfo {

    }

    record NotLoggedIn() implements GetApplicationInfo {

    }
    record ProgramNotFound() implements GetApplicationInfo {

    }

    record ApplicationNotFound() implements GetApplicationInfo {

    }

  }

  public sealed interface UpdateApplicationStatus {

    record Success(String status) implements UpdateApplicationStatus {

    }

    record ApplicationNotFound() implements UpdateApplicationStatus {

    }
    record ProgramNotFound() implements UpdateApplicationStatus {

    }

    record UserNotAdmin() implements UpdateApplicationStatus {

    }

    record NotLoggedIn() implements UpdateApplicationStatus {

    }

  }
}
