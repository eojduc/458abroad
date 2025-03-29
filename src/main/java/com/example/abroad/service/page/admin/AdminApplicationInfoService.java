package com.example.abroad.service.page.admin;

import com.example.abroad.model.Application;
import com.example.abroad.model.Program;
import com.example.abroad.model.User;
import com.example.abroad.model.Application.Document;
import com.example.abroad.model.Application.Note;
import com.example.abroad.model.Application.Status;
import com.example.abroad.service.ApplicationService;
import com.example.abroad.service.ApplicationService.Documents;
import com.example.abroad.service.FormatService;
import com.example.abroad.service.ProgramService;
import com.example.abroad.service.UserService;
import com.example.abroad.service.page.admin.AdminApplicationInfoService.PostNote.UserLacksPermission;
import jakarta.servlet.http.HttpSession;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;

@Service
public record AdminApplicationInfoService(
  FormatService formatService,
  UserService userService,
  ProgramService programService,
  ApplicationService applicationService
) {


  public GetApplicationInfo getApplicationInfo(Integer programId, String username, HttpSession session) {
    var user = userService.findUserFromSession(session).orElse(null);
    if (user == null) {
      return new GetApplicationInfo.NotLoggedIn();
    }
    var program = programService.findById(programId).orElse(null);
    if (program == null) {
      return new GetApplicationInfo.ProgramNotFound();
    }
    var isAdmin = userService.isAdmin(user);
    var isReviewer = userService.isReviewer(user);
    var isLead = programService.isFacultyLead(program, user);
    if (!isAdmin && !isReviewer && !isLead) {
      return new GetApplicationInfo.UserLacksPermission();
    }
    var student = userService.findByUsername(username).orElse(null);
    if (student == null) {
      return new GetApplicationInfo.ApplicationNotFound();
    }
    var application = applicationService.findByProgramAndStudent(program, student).orElse(null);
    if (application == null) {
      return new GetApplicationInfo.ApplicationNotFound();
    }
    var noteInfos = applicationService.getNotes(application)
      .stream()
      .sorted(Comparator.comparing(Note::timestamp).reversed())
      .map(this::getNoteInfo)
      .toList();
    var responses = applicationService.getResponses(application)
      .stream()
      .flatMap(response ->
          programService.findQuestion(program, response.question()).stream()
            .map(question -> new ResponseInfo(question.text(), response.response()))
      ).toList();
    var documents = getDocInfo(applicationService.getDocuments(application));
    var programIsPast = program.endDate().isBefore(LocalDate.now());
    var theme = user.theme().name().toLowerCase();
    var programDetails = getProgramDetails(program);
    var applicationDetails = getAppDetails(programIsPast, application, student, isAdmin, isReviewer, isLead);
    return new GetApplicationInfo.Success(noteInfos, documents, theme,
      responses, programDetails,
      applicationDetails, user.displayName(), isReviewer, getLetters(student, program), isAdmin);
  }

  public record ResponseInfo(String question, String response) {
  }

  public ProgramDetails getProgramDetails(Program program) {
    var facultyLeads = programService.findFacultyLeads(program)
      .stream()
      .map(formatService::displayUser)
      .toList();
    var programFields = List.of(
      new Field("Description", program.description()),
      new Field("Term", formatService.formatTerm(program.semester(), program.year())),
      new Field("Start Date", formatService.formatLocalDate(program.startDate())),
      new Field("End Date", formatService.formatLocalDate(program.endDate())),
      new Field("Essential Docs Due", formatService.formatLocalDate(program.documentDeadline())),
      new Field("Application Open", formatService.formatLocalDate(program.applicationOpen())),
      new Field("Application Close", formatService.formatLocalDate(program.applicationClose()))
    );
    return new ProgramDetails(
      program.id(),
      program.title(), facultyLeads, programFields);
  }

  public record Field(String name, String value) {

  }

  public record ProgramDetails(
    Integer id,
    String title, List<String> facultyLeads, List<Field> fields) {
  }


  public record StatusOption(String status, String displayedStatus) {
  }

  public StatusOption statusOption(Status status, Boolean programIsPast) {
    return switch (status) {
      case APPLIED -> new StatusOption(Status.APPLIED.name(), "Applied");
      case ELIGIBLE -> new StatusOption(Status.ELIGIBLE.name(), "Eligible");
      case APPROVED -> new StatusOption(Status.APPROVED.name(), "Approved");
      case ENROLLED -> new StatusOption(Status.ENROLLED.name(), programIsPast ? "Completed" : "Enrolled");
      case CANCELLED -> new StatusOption(Status.CANCELLED.name(), "Cancelled");
      case WITHDRAWN -> new StatusOption(Status.WITHDRAWN.name(), "Withdrawn");
    };
  }
  public List<StatusOption> statusChangeOptions(Boolean isAdmin, Boolean isReviewer, Boolean isLead, Boolean programIsPast) {
    if (isAdmin) {
      return Stream.of(
          Status.APPLIED, Status.ELIGIBLE, Status.APPROVED, Status.ENROLLED, Status.CANCELLED)
        .map(status -> statusOption(status, programIsPast)).toList();
    }
    if (isReviewer) {
      return Stream.of(Status.ELIGIBLE, Status.APPLIED)
        .map(status -> statusOption(status, programIsPast)).toList();
    }
    if (isLead) {
      return Stream.of(Status.ELIGIBLE, Status.APPLIED, Status.APPROVED)
        .map(status -> statusOption(status, programIsPast)).toList();
    }
    return List.of();
  }
  public List<StatusOption> getStatusOptions(Boolean programIsPast) {
    return List.of(
      new StatusOption(Status.APPLIED.name(), "Applied"),
      new StatusOption(Status.ENROLLED.name(), programIsPast ? "Completed" : "Enrolled"),
      new StatusOption(Status.CANCELLED.name(), "Cancelled"),
      new StatusOption(Status.ELIGIBLE.name(), "Eligible"),
      new StatusOption(Status.WITHDRAWN.name(), "Withdrawn"),
      new StatusOption(Status.APPROVED.name(), "Approved")
    );
  }

  public record ApplicationDetails(
    List<StatusOption> statusOptions,
    List<Field> fields,
    String studentDisplayName,
    String status
  ){}

  public ApplicationDetails getAppDetails(Boolean programIsPast, Application application, User student,
    Boolean isAdmin, Boolean isReviewer, Boolean isLead) {
    return new ApplicationDetails(statusChangeOptions(isAdmin, isReviewer, isLead, programIsPast),
      List.of(
        new Field("User", formatService.displayUser(student)),
        new Field ("Email", student.email()),
        new Field("Major", application.major()),
        new Field("GPA", application.gpa().toString()),
        new Field("Date of Birth", formatService.formatLocalDate(application.dateOfBirth()))
      ),
      formatService.displayUser(student),
      programIsPast && application.status() == Status.ENROLLED ? "COMPLETED" : application.status().toString()
    );
  }

  public record NoteInfo(String content, String origin) {
  }

  public NoteInfo getNoteInfo(Note note) {
    return new NoteInfo(note.content(), note.author() + " on " + formatService.formatInstant(note.timestamp()));
  }


  public List<DocumentInfo> getDocInfo(Documents documents){
    return List.of(
      new DocumentInfo(documents.assumptionOfRisk().isPresent(), Document.Type.ASSUMPTION_OF_RISK.name(), "Assumption of Risk",
        documents.assumptionOfRisk().map(doc -> formatService.formatInstant(doc.timestamp())).orElse("")),
      new DocumentInfo(documents.housing().isPresent(), Document.Type.HOUSING.name(), "Housing",
        documents.housing().map(doc -> formatService.formatInstant(doc.timestamp())).orElse("")),
      new DocumentInfo(documents.codeOfConduct().isPresent(), Document.Type.CODE_OF_CONDUCT.name(), "Code of Conduct",
        documents.codeOfConduct().map(doc -> formatService.formatInstant(doc.timestamp())).orElse("")),
      new DocumentInfo(documents.medicalHistory().isPresent(), Document.Type.MEDICAL_HISTORY.name(), "Medical History",
        documents.medicalHistory().map(doc -> formatService.formatInstant(doc.timestamp())).orElse(""))
    );
  }



  public UpdateApplicationStatus updateApplicationStatus(Integer programId,
    String username, Application.Status status, HttpSession session
  ) {
    var application = applicationService.findByProgramIdAndStudentUsername(programId, username).orElse(null);
    if (application == null) {
      return new UpdateApplicationStatus.ApplicationNotFound();
    }
    applicationService.updateStatus(application, status);
    var program = programService.findById(application.programId()).orElse(null);
    if (program == null) {
      return new UpdateApplicationStatus.ProgramNotFound();
    }
    var user = userService.findUserFromSession(session).orElse(null);
    if (user == null) {
      return new UpdateApplicationStatus.NotLoggedIn();
    }
    var isAdmin = userService.isAdmin(user);
    var isReviewer = userService.isReviewer(user);
    var isFacultyLead = programService.isFacultyLead(program, user);
    if (!isReviewer && !isFacultyLead && !isAdmin) {
      return new UpdateApplicationStatus.UserLacksPermission();
    }
    var adminStatuses = List.of(Status.ENROLLED, Status.CANCELLED, Status.ELIGIBLE, Status.APPROVED, Status.APPLIED);
    var facultyStatuses = List.of(Status.APPLIED, Status.ELIGIBLE, Status.APPROVED);
    var reviewerStatuses = List.of(Status.APPLIED, Status.ELIGIBLE);
    if (isAdmin && !adminStatuses.contains(status)) {
      return new UpdateApplicationStatus.UserLacksPermission();
    }
    if (isFacultyLead && !facultyStatuses.contains(status)) {
      return new UpdateApplicationStatus.UserLacksPermission();
    }
    if (isReviewer && !reviewerStatuses.contains(status)) {
      return new UpdateApplicationStatus.UserLacksPermission();
    }
    var programIsPast = program.endDate().isBefore(LocalDate.now());
    var displayedStatus = programIsPast && status == Status.ENROLLED ? "COMPLETED" : status.toString();
    System.out.println("Success");
    return new UpdateApplicationStatus.Success(displayedStatus);
  }

  public PostNote postNote(Integer programId, String student, String note, HttpSession session) {
    var user = userService.findUserFromSession(session).orElse(null);
    if (user == null) {
      return new PostNote.NotLoggedIn();
    }
    var application = applicationService.findByProgramIdAndStudentUsername(programId, student).orElse(null);
    if (application == null) {
      return new PostNote.ApplicationNotFound();
    }
    var program = programService.findById(programId).orElse(null);
    if (program == null) {
      return new PostNote.ApplicationNotFound();
    }
    var isAdmin = userService.isAdmin(user);
    var isReviewer = userService.isReviewer(user);
    var isFacultyLead = programService.isFacultyLead(program, user);
    if (!isReviewer && !isFacultyLead && !isAdmin) {
      return new PostNote.UserLacksPermission();
    }
    applicationService.saveNote(new Note(
      application.programId(),
      application.student(),
      user.username(),
      note,
      Instant.now()
    ));
    var notes = applicationService.getNotes(application)
      .stream()
      .sorted(Comparator.comparing(Note::timestamp))
      .map(this::getNoteInfo)
      .toList()
      .reversed();
    return new PostNote.Success(notes);
  }
  public sealed interface PostNote {
    record Success(List<NoteInfo> noteInfos) implements PostNote {
    }
    record ApplicationNotFound() implements PostNote {
    }

    record UserLacksPermission() implements PostNote {
    }

    record NotLoggedIn() implements PostNote {
    }
  }


  public record LetterInfo(String email, String name, Boolean submitted, Integer code, String timestamp) {
  }

  public List<LetterInfo> getLetters(User student, Program program) {
    return applicationService.getRecommendationRequests(program, student)
      .stream()
      .map(request -> {
        var letter = applicationService.findLetterOfRecommendation(request.programId(), request.student(), request.email());
        var timestamp = letter.map(doc -> formatService.formatInstant(doc.timestamp())).orElse("");
        return new LetterInfo(request.email(), request.name(), letter.isPresent(), request.code(), timestamp);
      }).toList();
  }
  public record DocumentInfo(Boolean isPresent, String type, String name, String timestamp) {
  }

  public sealed interface GetApplicationInfo  {

    record Success(List<NoteInfo> noteInfos,
                   List<DocumentInfo> documentInfos, String theme,
                    List<ResponseInfo> responses,
                   ProgramDetails programDetails, ApplicationDetails applicationDetails, String displayName, Boolean isReviewer,
                   List<LetterInfo> requests, Boolean isAdmin
    ) implements
      GetApplicationInfo {

    }

    record UserLacksPermission() implements GetApplicationInfo {

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

    record UserLacksPermission() implements UpdateApplicationStatus {

    }
    record UserNotAdmin() implements UpdateApplicationStatus {

    }

    record NotLoggedIn() implements UpdateApplicationStatus {

    }

  }
}
