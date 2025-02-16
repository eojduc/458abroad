package com.example.abroad.service.page;

import com.example.abroad.model.Program;
import com.example.abroad.model.Program.Semester;
import com.example.abroad.model.User;
import com.example.abroad.service.ProgramService;
import com.example.abroad.service.ProgramService.SaveProgram;
import com.example.abroad.service.UserService;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.Year;
import org.springframework.stereotype.Service;

@Service
public record AddProgramService(UserService userService, ProgramService programService) {

  public GetAddProgramInfo getAddProgramInfo(HttpSession session) {
    var user = userService.findUserFromSession(session).orElse(null);
    if (user == null) {
      return new GetAddProgramInfo.NotLoggedIn();
    }
    if (user.role() != User.Role.ADMIN) {
      return new GetAddProgramInfo.UserNotAdmin();
    }
    return new GetAddProgramInfo.Success(user);
  }

  public AddProgramInfo addProgramInfo(String title, String description,
      Integer year, LocalDate startDate, LocalDate endDate,
      Semester semester, LocalDate applicationOpen, LocalDate applicationClose,
      HttpSession session
  ) {
    var user = userService.findUserFromSession(session).orElse(null);
    if (user == null) {
      return new AddProgramInfo.NotLoggedIn();
    }
    if (user.role() != User.Role.ADMIN) {
      return new AddProgramInfo.UserNotAdmin();
    }
    Program program = new Program();
    program.setTitle(title);
    program.setYear(Year.of(year));
    program.setSemester(semester);
    program.setApplicationOpen(applicationOpen);
    program.setApplicationClose(applicationClose);
    program.setStartDate(startDate);
    program.setEndDate(endDate);
    program.setDescription(description);

   return switch (programService.saveProgram(program)) {
     case SaveProgram.TitleInvalid() -> new AddProgramInfo.InvalidProgramInfo("Title is invalid");
      case SaveProgram.DescriptionInvalid() -> new AddProgramInfo.InvalidProgramInfo("Description is invalid");
      case SaveProgram.ApplicationOpenAfterClose() -> new AddProgramInfo.InvalidProgramInfo("Application open is after close");
      case SaveProgram.ApplicationCloseAfterDocumentDeadline() -> new AddProgramInfo.InvalidProgramInfo("Application close is after document deadline");
      case SaveProgram.DocumentDeadlineAfterStart() -> new AddProgramInfo.InvalidProgramInfo("Document deadline is after start date");
      case SaveProgram.StartAfterEnd() -> new AddProgramInfo.InvalidProgramInfo("Start date is after end date");
      case SaveProgram.Success(var p) -> new AddProgramInfo.Success(p.id());
    };
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
  }

}
