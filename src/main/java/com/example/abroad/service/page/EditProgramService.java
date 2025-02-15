package com.example.abroad.service.page;

import com.example.abroad.model.Program;
import com.example.abroad.model.Program.Semester;
import com.example.abroad.model.User;
import com.example.abroad.respository.ProgramRepository;
import com.example.abroad.service.UserService;
import com.example.abroad.service.page.EditProgramService.UpdateProgramInfo.DatabaseError;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.Year;
import org.springframework.stereotype.Service;

@Service
public record EditProgramService(UserService userService, ProgramRepository programRepository) {

  public GetEditProgramInfo getEditProgramInfo(Integer programId, HttpSession session) {
    var user = userService.findUserFromSession(session).orElse(null);
    if (user == null) {
      return new GetEditProgramInfo.NotLoggedIn();
    }
    if (user.role() != User.Role.ADMIN) {
      return new GetEditProgramInfo.UserNotAdmin();
    }
    var program = programRepository.findById(programId).orElse(null);
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
      if (title.length() > 80) {
        return new UpdateProgramInfo.TitleTooLong();
      }
      if (startDate.isAfter(endDate) || applicationClose.isAfter(startDate) ||
        applicationOpen.isAfter(applicationClose)) {
        return new UpdateProgramInfo.IncoherentDates();
      }
      var user = userService.findUserFromSession(session).orElse(null);
      if (user == null) {
        return new UpdateProgramInfo.NotLoggedIn();
      }
      if (user.role() != User.Role.ADMIN) {
        return new UpdateProgramInfo.UserNotAdmin();
      }
      var program = programRepository.findById(programId).orElse(null);
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

      programRepository.save(program);
      return new UpdateProgramInfo.Success();
    } catch (Exception e) {
      return new DatabaseError(e.getMessage());
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
    record IncoherentDates() implements UpdateProgramInfo { }
    record TitleTooLong() implements UpdateProgramInfo { }
    record DatabaseError(String messsge) implements UpdateProgramInfo { }
  }

}
