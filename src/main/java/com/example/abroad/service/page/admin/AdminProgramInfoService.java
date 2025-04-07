package com.example.abroad.service.page.admin;

import com.example.abroad.model.Application;
import com.example.abroad.model.Program;
import com.example.abroad.model.Program.Partner;
import com.example.abroad.model.User;
import com.example.abroad.model.Application.Document;
import com.example.abroad.model.Application.Note;
import com.example.abroad.model.Application.Status;
import com.example.abroad.service.ApplicationService;
import com.example.abroad.service.ApplicationService.Documents;
import com.example.abroad.service.AuditService;
import com.example.abroad.service.FormatService;
import com.example.abroad.service.ProgramService;
import com.example.abroad.service.UserService;
import com.example.abroad.service.page.admin.AdminProgramInfoService.DeleteProgram.ProgramNotFound;
import com.example.abroad.service.page.admin.AdminProgramInfoService.DeleteProgram.Success;
import com.example.abroad.service.page.admin.AdminProgramInfoService.DeleteProgram.UserLacksPermission;
import com.example.abroad.service.page.admin.AdminProgramInfoService.DeleteProgram.UserNotAdmin;
import com.example.abroad.service.page.admin.AdminProgramInfoService.DeleteProgram.UserNotFound;
import com.example.abroad.view.Badge;
import com.example.abroad.view.Field;
import jakarta.servlet.http.HttpSession;
import java.text.DecimalFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public record AdminProgramInfoService(
  ProgramService programService,
  ApplicationService applicationService,
  UserService userService,
  FormatService formatService,
  AuditService auditService
) {
  private static final Logger logger = LoggerFactory.getLogger(AdminProgramInfoService.class);

  public DeleteProgram deleteProgram(Integer programId, HttpSession session) {
    var user = userService.findUserFromSession(session).orElse(null);
    if (user == null) {
      return new UserNotFound();
    }

    var program = programService.findById(programId).orElse(null);
    if (program == null) {
      return new ProgramNotFound();
    }
    var isAdmin = userService.isAdmin(user);
    var isReviewer = userService.isReviewer(user);
    var isFacultyLead = userService.isFaculty(user) &&
      programService.findFacultyLeads(program).stream()
          .map(User::username)
          .anyMatch(username -> username.equals(user.username()));
    if (!isAdmin && !isReviewer && !isFacultyLead) {
      return new UserLacksPermission();
    }
    programService.deleteProgram(program);
    auditService.logEvent(String.format("Deleted program %s", program.title()));
    return new Success();
  }
  public record ProgramDetails(
    String title,
    String description,
    List<Field> fields, List<String> facultyLeads, List<String> partners) {
  }

  public ProgramDetails getProgramDetails(Program program) {
    var fields = new ArrayList<>(List.of(
      new Field("Term", formatService.formatTerm(program.semester(), program.year())),
      new Field("Application Opens", formatService.formatLocalDate(program.applicationOpen())),
      new Field("Application Closes", formatService.formatLocalDate(program.applicationClose())),
      new Field("Document Deadline", formatService.formatLocalDate(program.documentDeadline())),
      new Field("Start Date", formatService.formatLocalDate(program.startDate())),
      new Field("End Date", formatService.formatLocalDate(program.endDate())),
      new Field("Track Payment", program.trackPayment() ? "Yes" : "No")
    ));
    if (program.trackPayment()) {
      fields.add(new Field("Payment Deadline", formatService.formatLocalDate(program.paymentDeadline())));
    }
    var facultyLeads = programService.findFacultyLeads(program)
      .stream().map(formatService::displayUser)
      .toList();
    var partners = programService.getPartners(program)
      .stream()
      .map(Partner::username)
      .flatMap(s -> userService.findByUsername(s).stream())
      .map(formatService::displayUser)
      .toList();
    return new ProgramDetails(program.title(), program.description(), fields, facultyLeads, partners);
  }
  public sealed interface GetProgramInfo {

    record Success(Program program, List<ApplicantInfo> applicants, User user, Boolean documentDeadlinePassed, Boolean programIsDone, List<? extends User> facultyLeads,
                   ProgramDetails programDetails, ApplicantDetails applicantDetails, Boolean canSeeApplicants) implements
      GetProgramInfo {

    }

    record ProgramNotFound() implements GetProgramInfo {

    }

    record UserNotFound() implements GetProgramInfo {

    }

    record UserNotAdmin() implements GetProgramInfo {

    }

    record UserLacksPermission() implements GetProgramInfo {

    }
  }

  public record ApplicantDetails(List<Field> headers) {

  }

  public GetProgramInfo getProgramInfo(Integer programId, HttpSession session,
    Optional<Column> column,
    Optional<Filter> filter,
    Optional<Sort> sort) {
    var user = userService.findUserFromSession(session).orElse(null);
    if (user == null) {
      return new GetProgramInfo.UserNotFound();
    }
    var program = programService.findById(programId).orElse(null);
    if (program == null) {
      return new GetProgramInfo.ProgramNotFound();
    }
    var isAdmin = userService.isAdmin(user);
    var isReviewer = userService.isReviewer(user);
    var isFacultyLead = userService.isFaculty(user) &&
      programService.findFacultyLeads(program).stream()
        .map(User::username)
        .anyMatch(username -> username.equals(user.username()));
    var isFaculty = userService.isFaculty(user);
    if (!isAdmin && !isReviewer && !isFaculty) {
      return new GetProgramInfo.UserLacksPermission();
    }
    var canSeeApplicants = isAdmin || isReviewer || isFacultyLead;
    Comparator<Applicant> sorter = switch (column.orElse(Column.NONE)) {
      case USERNAME, NONE -> Comparator.comparing(Applicant::username);
      case DISPLAY_NAME -> Comparator.comparing(Applicant::displayName);
      case EMAIL -> Comparator.comparing(Applicant::email);
      case MAJOR -> Comparator.comparing(Applicant::major);
      case GPA -> Comparator.comparing(Applicant::gpa);
      case DOB -> Comparator.comparing(Applicant::dob);
      case STATUS -> Comparator.comparing(Applicant::status);
      case NOTE_COUNT -> Comparator.comparing(Applicant::noteCount);
      case LATEST_NOTE -> Comparator.comparing(applicant -> applicant.latestNote().map(Note::timestamp).orElse(Instant.MIN));
      case MEDICAL_HISTORY -> Comparator.comparing(applicant -> applicant.documents().medicalHistory().map(Document::timestamp).orElse(Instant.MIN));
      case CODE_OF_CONDUCT -> Comparator.comparing(applicant -> applicant.documents().codeOfConduct().map(Document::timestamp).orElse(Instant.MIN));
      case ASSUMPTION_OF_RISK -> Comparator.comparing(applicant -> applicant.documents().assumptionOfRisk().map(Document::timestamp).orElse(Instant.MIN));
      case HOUSING -> Comparator.comparing(applicant -> applicant.documents().housing().map(Document::timestamp).orElse(Instant.MIN));
      case PAYMENT_STATUS -> Comparator.comparing(Applicant::paymentStatus);
    };
    var reversed = sort.orElse(Sort.ASCENDING) == Sort.DESCENDING;
    var programIsDone = program.endDate().isBefore(LocalDate.now());
    Predicate<Applicant> filterer = switch (filter.orElse(Filter.ALL)) {
      case APPLIED -> applicant -> applicant.status() == Status.APPLIED;
      case ENROLLED -> applicant -> applicant.status() == Status.ENROLLED && !programIsDone;
      case CANCELLED -> applicant -> applicant.status() == Status.CANCELLED;
      case WITHDRAWN -> applicant -> applicant.status() == Status.WITHDRAWN;
      case ALL -> applicant -> true;
      case COMPLETED -> applicant -> applicant.status() == Status.ENROLLED && programIsDone;
      case ELIGIBLE -> applicant -> applicant.status() == Status.ELIGIBLE;
      case APPROVED -> applicant -> applicant.status() == Status.APPROVED;
    };

    var documentDeadlinePassed = LocalDate.now().isAfter(program.documentDeadline());
    var users = userService.findAll();
    var applicants = applicationService.findByProgram(program).stream()
      .flatMap(application -> applicants(users.stream(), application))
      .sorted(reversed ? sorter.reversed() : sorter)
      .filter(filterer)
      .map(applicant -> getApplicantInfo(applicant, documentDeadlinePassed, programId, programIsDone, isAdmin, isReviewer, isFacultyLead))
      .toList();
    var programDetails = getProgramDetails(program);
    var facultyLeads = programService.findFacultyLeads(program);
    var applicantDetails = getApplicantDetails(program);
    return new GetProgramInfo.Success(program, applicants, user, documentDeadlinePassed, programIsDone, facultyLeads, programDetails, applicantDetails, canSeeApplicants);
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

  public ApplicantDetails getApplicantDetails(Program program) {
    var fields = new ArrayList<Field>(List.of(
      new Field("Name", "NAME"),
      new Field("Username", "USERNAME"),
      new Field("Email", "EMAIL"),
      new Field("Major", "MAJOR"),
      new Field("GPA", "GPA"),
      new Field("Date of Birth", "DOB"),
      new Field("Status", "STATUS"),
      new Field("Medical History", "MEDICAL_HISTORY"),
      new Field("Code of Conduct", "CODE_OF_CONDUCT"),
      new Field("Assumption of Risk", "ASSUMPTION_OF_RISK"),
      new Field("Housing", "HOUSING"),
      new Field("Note Count", "NOTE_COUNT"),
      new Field("Latest Note", "LATEST_NOTE")
    ));
    if (program.trackPayment()) {
      fields.add(new Field("Payment Status", "PAYMENT_STATUS"));
    }
    return new ApplicantDetails(fields);
  }


  private Stream<Applicant> applicants(Stream<? extends User> students, Application application) {
    var documents = applicationService.getDocuments(application);
    var notes = applicationService.getNotes(application);
    var program = programService.findById(application.programId()).orElse(null);
    var displayStatus = switch (application.status()) {
      case ENROLLED -> program.endDate().isBefore(LocalDate.now()) ? "COMPLETED" : "ENROLLED";
      default -> application.status().toString();
    };
    logger.debug("Student: {}", application.student());
    return students.filter(student -> student.username().equals(application.student()))
      .map(student -> new Applicant(
        student.username(),
        student.displayName(),
        student.email(),
        application.major(),
        application.gpa(),
        application.dateOfBirth(),
        application.status(),
        documents,
        notes.size(),
        notes.stream().max(Comparator.comparing(Note::timestamp)),
        displayStatus,
        application.paymentStatus().name()
      )
    );
  }

  public enum Filter {
    APPLIED, ENROLLED, CANCELLED, WITHDRAWN, ALL, COMPLETED, ELIGIBLE, APPROVED
  }

  public enum Column {
    USERNAME, DISPLAY_NAME, EMAIL, MAJOR, GPA, DOB, STATUS, NONE, NOTE_COUNT, LATEST_NOTE, MEDICAL_HISTORY,
    CODE_OF_CONDUCT, ASSUMPTION_OF_RISK, HOUSING, PAYMENT_STATUS
  }
  public enum Sort {
    ASCENDING, DESCENDING
  }

  public sealed interface DeleteProgram {

    record Success() implements DeleteProgram {

    }

    record ProgramNotFound() implements DeleteProgram {

    }

    record UserNotFound() implements DeleteProgram {

    }

    record UserNotAdmin() implements DeleteProgram {

    }
    record UserLacksPermission() implements DeleteProgram {

    }
  }
  public DocumentInfo getDocumentInfo(Optional<Document> document, Boolean deadlinePassed) {
    return document.map(doc -> new DocumentInfo("SUBMITTED",
      String.format("%s at %s", doc.timestamp().atZone(ZoneId.systemDefault()).toLocalDate(), doc.timestamp().atZone(ZoneId.systemDefault()).toLocalTime())))
      .orElse(deadlinePassed ?
        new DocumentInfo("MISSING", "Missing")
        : new DocumentInfo("PENDING", "Pending"));

  }

  public record DocumentInfo(String status, String text) {
  }
  public record ApplicantInfo(
    String username,
    String displayName,
    String email,
    String major,
    String gpa,
    String dateOfBirth,
    String url,
    String status,
    List<DocumentInfo> documents,
    List<StatusOption> statusOptions,
    Integer noteCount,
    String latestNote,
    String displayStatus,
    String paymentStatus
    ) {
  }

  public record StatusOption(String value, String text){ }

  public ApplicantInfo getApplicantInfo(Applicant applicant, Boolean deadlinePassed, Integer programId, Boolean programIsDone,
    Boolean isAdmin, Boolean isReviewer, Boolean isLead
    ) {
    return new ApplicantInfo(
      applicant.username(), applicant.displayName(),
      applicant.email(), applicant.major(),
      new DecimalFormat("#.##").format(applicant.gpa()),
      applicant.dob().toString(),
      String.format("/admin/applications/%d/%s", programId, applicant.username()),
      applicant.status().name(),
      List.of(
        getDocumentInfo(applicant.documents.medicalHistory(), deadlinePassed),
        getDocumentInfo(applicant.documents.codeOfConduct(), deadlinePassed),
        getDocumentInfo(applicant.documents.assumptionOfRisk(), deadlinePassed),
        getDocumentInfo(applicant.documents.housing(), deadlinePassed)
     ),
      statusChangeOptions(isAdmin, isReviewer, isLead, programIsDone),
      applicant.noteCount(),
      applicant.latestNote().map(note -> note.author() + " on " + formatService.formatInstant(note.timestamp())).orElse(""),
      applicant.displayStatus(),
      applicant.paymentStatus());
  }
  public record Applicant(String username, String displayName, String email, String major,
                          Double gpa, LocalDate dob, Status status,
                          Documents documents,
                          Integer noteCount,
                          Optional<Note> latestNote,
                          String displayStatus,
                          String paymentStatus) {
  }

}
