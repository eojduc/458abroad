package com.example.abroad.service.page.admin;

import com.example.abroad.model.Program;
import com.example.abroad.model.Program.Semester;
import com.example.abroad.model.User;
import com.example.abroad.service.AuditService;
import com.example.abroad.service.ProgramService;
import com.example.abroad.service.ProgramService.SaveProgram;
import com.example.abroad.service.UserService;
import com.example.abroad.service.page.admin.AddProgramService.AddProgramInfo;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public record EditProgramService(UserService userService, ProgramService programService, AuditService auditService) {

  public EditProgramPage getEditProgramInfo(Integer programId, HttpSession session) {
    var user = userService.findUserFromSession(session).orElse(null);
    if (user == null) {
      return new EditProgramPage.NotLoggedIn();
    }
    if (!userService.isAdmin(user)) {
      return new EditProgramPage.UserNotAdmin();
    }
    var program = programService.findById(programId).orElse(null);
    if (program == null) {
      return new EditProgramPage.ProgramNotFound();
    }
    var facultyLeads = programService.findFacultyLeads(program)
      .stream()
      .toList();

    var facultyUsernames = facultyLeads.stream()
      .map(User::username)
      .toList();

    var nonFacultyLeads = userService.findAll().stream()
      .filter(userService::isAdmin)
      .filter(u -> !facultyUsernames.contains(u.username()))
      .toList();

    List<String> questions = programService.getQuestions(program).stream()
      .map(Program.Question::text)
      .toList();

    var applicantsExists = !programService.getApplications(program).isEmpty();

    return new EditProgramPage.Success(program, user, facultyLeads, nonFacultyLeads, questions, applicantsExists);
  }

  public UpdateProgramInfo updateProgramInfo(
    Integer programId, String title, String description, Integer year, LocalDate startDate,
    LocalDate endDate, Semester semester, LocalDate applicationOpen, LocalDate applicationClose,
    List<String> facultyLeads, LocalDate documentDeadline, List<String> selectedQuestions, HttpSession session
  ) {
    var user = userService.findUserFromSession(session).orElse(null);
    if (user == null) {
      return new UpdateProgramInfo.NotLoggedIn();
    }
    if (!userService.isAdmin(user)) {
      return new UpdateProgramInfo.UserNotAdmin();
    }
    var program = programService.findById(programId).orElse(null);
    if (program == null) {
      return new UpdateProgramInfo.ProgramNotFound();
    }
    var newProgram = program
        .withTitle(title)
        .withYear(Year.of(year))
        .withSemester(semester)
        .withApplicationOpen(applicationOpen)
        .withApplicationClose(applicationClose)
        .withStartDate(startDate)
        .withEndDate(endDate)
        .withDescription(description)
        .withDocumentDeadline(documentDeadline);
    var leadUsers = userService.findAll().stream()
        .filter(u -> facultyLeads.contains(u.username()))
        .toList();
    return switch (programService.addProgram(newProgram, leadUsers, selectedQuestions)) {
      case SaveProgram.InvalidProgramInfo(var message) -> new UpdateProgramInfo.InvalidProgramInfo(message);
      case SaveProgram.Success(var prog) -> {
        auditService.logEvent(String.format("Program %s(%d) updated by %s", prog.title(), prog.id(), user.username()));
        yield new UpdateProgramInfo.Success();
      }
      case SaveProgram.DatabaseError(var message) -> new UpdateProgramInfo.DatabaseError(message);
    };
  }



  public sealed interface EditProgramPage {
    record Success(
      Program program,
      User admin,
      List<? extends User> facultyLeads,
      List<? extends User> nonFacultyLeads,
      List<String> currentQuestions,
      Boolean applicantsExist
    ) implements EditProgramPage { }
    record ProgramNotFound() implements EditProgramPage { }
    record UserNotAdmin() implements EditProgramPage { }
    record NotLoggedIn() implements EditProgramPage { }
  }

  public sealed interface UpdateProgramInfo {
    record Success() implements UpdateProgramInfo { }
    record ProgramNotFound() implements UpdateProgramInfo { }
    record UserNotAdmin() implements UpdateProgramInfo { }
    record NotLoggedIn() implements UpdateProgramInfo { }
    record InvalidProgramInfo(String message) implements UpdateProgramInfo { }
    record DatabaseError(String messsge) implements UpdateProgramInfo { }
  }

}
