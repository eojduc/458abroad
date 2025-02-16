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
    return new GetEditProgramInfo.Success(program, user);
  }

  public UpdateProgramInfo updateProgramInfo(Integer programId,
    String title, String description,
    Integer year, LocalDate startDate, LocalDate endDate,
    Semester semester, LocalDate applicationOpen, LocalDate applicationClose,
    HttpSession session
  ) {
    try {
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
      return switch (programService.saveProgram(program)) {
        case SaveProgram.Success(var p) -> new UpdateProgramInfo.Success();
        case SaveProgram.ApplicationCloseAfterDocumentDeadline() -> new UpdateProgramInfo.DateIncoherence();
        case SaveProgram.ApplicationOpenAfterClose() -> new UpdateProgramInfo.DateIncoherence();
        case SaveProgram.DescriptionInvalid() -> new UpdateProgramInfo.DescriptionInvalid();
        case SaveProgram.DocumentDeadlineAfterStart() -> new UpdateProgramInfo.DateIncoherence();
        case SaveProgram.StartAfterEnd() -> new UpdateProgramInfo.DateIncoherence();
        case SaveProgram.TitleInvalid() -> new UpdateProgramInfo.TitleInvalid();
      };
    } catch (Exception e) {
      return new UpdateProgramInfo.DatabaseError(e.getMessage());
    }
  }


  public sealed interface GetEditProgramInfo {
    record Success(Program program, User admin) implements GetEditProgramInfo { }
    record ProgramNotFound() implements GetEditProgramInfo { }
    record UserNotAdmin() implements GetEditProgramInfo { }
    record NotLoggedIn() implements GetEditProgramInfo { }
  }

  public sealed interface UpdateProgramInfo {
    record Success() implements UpdateProgramInfo { }
    record ProgramNotFound() implements UpdateProgramInfo { }
    record UserNotAdmin() implements UpdateProgramInfo { }
    record NotLoggedIn() implements UpdateProgramInfo { }
    record TitleInvalid() implements UpdateProgramInfo { }
    record DescriptionInvalid() implements UpdateProgramInfo { }
    record DateIncoherence() implements UpdateProgramInfo { }
    record DatabaseError(String messsge) implements UpdateProgramInfo { }
  }

}
