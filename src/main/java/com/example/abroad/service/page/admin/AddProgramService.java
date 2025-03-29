package com.example.abroad.service.page.admin;

import com.example.abroad.model.Program;
import com.example.abroad.model.Program.Semester;
import com.example.abroad.model.User;
import com.example.abroad.service.AuditService;
import com.example.abroad.service.ProgramService;
import com.example.abroad.service.ProgramService.SaveProgram;
import com.example.abroad.service.UserService;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public record AddProgramService(UserService userService, ProgramService programService,
                                AuditService auditService) {

  static List<String> DEFAULT_QUESTIONS = List.of(
    "Why do you want to participate in this study abroad program?",
    "How does this program align with your academic or career goals?",
    "What challenges do you anticipate during this experience, and how will you address them?",
    "Describe a time you adapted to a new or unfamiliar environment.",
    "What unique perspective or contribution will you bring to the group?"
  );

  public GetAddProgramInfo getAddProgramInfo(HttpSession session) {
    var user = userService.findUserFromSession(session).orElse(null);
    if (user == null) {
      return new GetAddProgramInfo.NotLoggedIn();
    }
    if (!userService.isAdmin(user)) {
      return new GetAddProgramInfo.UserNotAdmin();
    }
    return new GetAddProgramInfo.Success(user);
  }

  public AddProgramInfo addProgramInfo(
    String title, String description, List<String> facultyLeads, Integer year, LocalDate startDate, LocalDate endDate, LocalDate essentialDocsDate,
    Semester semester, LocalDate applicationOpen, LocalDate applicationClose, List<String> questions, HttpSession session
  ) {
    var user = userService.findUserFromSession(session).orElse(null);
    if (user == null) {
      return new AddProgramInfo.NotLoggedIn();
    }
    if (!userService.isAdmin(user)) {
      return new AddProgramInfo.UserNotAdmin();
    }
    Program program = new Program(
      null, title, Year.of(year), semester, applicationOpen, applicationClose,
      essentialDocsDate, startDate, endDate, description
    );
    var leadUsers = userService.findAll().stream()
        .filter(u -> facultyLeads.contains(u.username()))
        .toList();
   return switch (programService.addProgram(program, leadUsers, questions)) {
     case SaveProgram.InvalidProgramInfo(var message) -> new AddProgramInfo.InvalidProgramInfo(message);
     case SaveProgram.Success(var prog) -> {
        auditService.logEvent(String.format("Program %s(%d) added by %s", prog.title(), prog.id(), user.username()));
       yield new AddProgramInfo.Success(prog.id());
     }
     case SaveProgram.DatabaseError(var message) -> new AddProgramInfo.DatabaseError(message);
    };
  }

  public List<? extends User> getFacultyList() {
    return userService.findUsersWithRole(User.Role.Type.FACULTY);
  }
  public List<String> getDefaultQuestions() {
    return DEFAULT_QUESTIONS;
  }


  public sealed interface GetAddProgramInfo {

    record Success(User admin) implements GetAddProgramInfo {

    }

    record UserNotAdmin() implements GetAddProgramInfo {

    }

    record NotLoggedIn() implements GetAddProgramInfo {

    }
  }

  public sealed interface AddProgramInfo {

    record Success(Integer programId) implements AddProgramInfo {

    }

    record UserNotAdmin() implements AddProgramInfo {

    }

    record NotLoggedIn() implements AddProgramInfo {

    }

    record InvalidProgramInfo(String message) implements AddProgramInfo {

    }
    record DatabaseError(String message) implements AddProgramInfo {

    }
  }

}
