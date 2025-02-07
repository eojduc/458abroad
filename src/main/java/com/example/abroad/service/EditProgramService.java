package com.example.abroad.service;

import com.example.abroad.Config;
import com.example.abroad.model.Admin;
import com.example.abroad.model.Program;
import com.example.abroad.model.Program.Semester;
import com.example.abroad.respository.ProgramRepository;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import org.springframework.stereotype.Service;

@Service
public record EditProgramService(UserService userService, ProgramRepository programRepository) {

  public GetEditProgramInfo getEditProgramInfo(Integer programId, HttpSession session) {
    var user = userService.getUser(session).orElse(null);
    if (user == null) {
      return new GetEditProgramInfo.NotLoggedIn();
    }
    if (!(user instanceof Admin admin)) {
      return new GetEditProgramInfo.UserNotAdmin();
    }
    var program = programRepository.findById(programId).orElse(null);
    if (program == null) {
      return new GetEditProgramInfo.ProgramNotFound();
    }
    return new GetEditProgramInfo.Success(program, admin);
  }

  public UpdateProgramInfo updateProgramInfo(Integer programId,
    String title, String description,
    Integer year, LocalDate startDate, LocalDate endDate,
    String facultyLead,
    Semester semester, LocalDateTime applicationOpen, LocalDateTime applicationClose,
    HttpSession session
  ) {
    if (title.length() > 80) {
      return new UpdateProgramInfo.TitleTooLong();
    }
    if (startDate.isAfter(endDate) || applicationClose.isAfter(startDate.atStartOfDay()) ||
      applicationOpen.isAfter(applicationClose)) {
      return new UpdateProgramInfo.IncoherentDates();
    }
    var user = userService.getUser(session).orElse(null);
    if (user == null) {
      return new UpdateProgramInfo.NotLoggedIn();
    }
    if (!(user instanceof Admin)) {
      return new UpdateProgramInfo.UserNotAdmin();
    }
    var program = programRepository.findById(programId).orElse(null);
    if (program == null) {
      return new UpdateProgramInfo.ProgramNotFound();
    }
    program.setTitle(title);
    program.setYear(Year.of(year));
    program.setSemester(semester);
    program.setApplicationOpen(applicationOpen.atZone(Config.ZONE_ID).toInstant());
    program.setApplicationClose(applicationClose.atZone(Config.ZONE_ID).toInstant());
    program.setStartDate(startDate);
    program.setEndDate(endDate);
    program.setFacultyLead(facultyLead);
    program.setDescription(description);

    programRepository.save(program);
    return new UpdateProgramInfo.Success();
  }


  public sealed interface GetEditProgramInfo {
    record Success(Program program, Admin admin) implements GetEditProgramInfo { }
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
  }

}
