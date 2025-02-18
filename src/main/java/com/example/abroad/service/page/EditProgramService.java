package com.example.abroad.service.page;

import com.example.abroad.model.Program;
import com.example.abroad.model.Program.FacultyLead;
import com.example.abroad.model.Program.Semester;
import com.example.abroad.model.User;
import com.example.abroad.service.ProgramService;
import com.example.abroad.service.ProgramService.SaveProgram;
import com.example.abroad.service.UserService;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public record EditProgramService(UserService userService, ProgramService programService) {

  public GetEditProgramInfo getEditProgramInfo(Integer programId, HttpSession session) {
    var user = userService.findUserFromSession(session).orElse(null);
    if (user == null) {
      return new GetEditProgramInfo.NotLoggedIn();
    }
    if (user.role() != User.Role.ADMIN) {
      return new GetEditProgramInfo.UserNotAdmin();
    }
    var program = programService.findById(programId).orElse(null);
    if (program == null) {
      return new GetEditProgramInfo.ProgramNotFound();
    }
    var facultyLeads = programService.findFacultyLeads(program)
      .stream()
      .toList();

    var facultyUsernames = facultyLeads.stream()
      .map(User::username)
      .toList();

    var nonFacultyLeads = userService.findAll().stream()
      .filter(User::isAdmin)
      .filter(u -> !facultyUsernames.contains(u.username()))
      .toList();

    return new GetEditProgramInfo.Success(program, user, facultyLeads, nonFacultyLeads);
  }

  public UpdateProgramInfo updateProgramInfo(
    Integer programId, String title, String description, Integer year, LocalDate startDate,
    LocalDate endDate, Semester semester, LocalDate applicationOpen, LocalDate applicationClose,
    List<String> facultyLeads, HttpSession session, LocalDate documentDeadline
  ) {
    var user = userService.findUserFromSession(session).orElse(null);
    if (user == null) {
      return new UpdateProgramInfo.NotLoggedIn();
    }
    if (user.role() != User.Role.ADMIN) {
      return new UpdateProgramInfo.UserNotAdmin();
    }
    var program = programService.findById(programId).orElse(null);
    if (program == null) {
      return new UpdateProgramInfo.ProgramNotFound();
    }
    program.setTitle(title);
    program.setYear(Year.of(year));
    program.setSemester(semester);
    program.setApplicationOpen(applicationOpen);
    program.setApplicationClose(applicationClose);
    program.setStartDate(startDate);
    program.setEndDate(endDate);
    program.setDescription(description);
    program.setDocumentDeadline(documentDeadline);
    return switch (programService.saveProgram(program)) {
      case SaveProgram.Success(var prog) -> {
        programService.setFacultyLeads(prog, facultyLeads);
        yield new UpdateProgramInfo.Success();
      }
      case SaveProgram.InvalidProgramInfo(var message) -> new UpdateProgramInfo.InvalidProgramInfo(message);
      case SaveProgram.DatabaseError(var message) -> new UpdateProgramInfo.DatabaseError(message);
    };
  }


  public sealed interface GetEditProgramInfo {
    record Success(
      Program program,
      User admin,
      List<? extends User> facultyLeads,
      List<? extends User> nonFacultyLeads
    ) implements GetEditProgramInfo { }
    record ProgramNotFound() implements GetEditProgramInfo { }
    record UserNotAdmin() implements GetEditProgramInfo { }
    record NotLoggedIn() implements GetEditProgramInfo { }
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
