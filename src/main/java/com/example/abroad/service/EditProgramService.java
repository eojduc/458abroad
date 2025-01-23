package com.example.abroad.service;

import com.example.abroad.Config;
import com.example.abroad.model.Admin;
import com.example.abroad.model.Program;
import com.example.abroad.model.Program.Semester;
import com.example.abroad.respository.ProgramRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import org.springframework.stereotype.Service;

@Service
public record EditProgramService(UserService userService, ProgramRepository programRepository) {

  public GetEditProgramInfo getEditProgramInfo(Integer programId, HttpServletRequest request) {
    var user = userService.getUser(request).orElse(null);
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
     String title,  String description,
     Integer year,  LocalDate startDate, LocalDate endDate,
     String facultyLead,
     Semester semester,  LocalDateTime applicationOpen,  LocalDateTime applicationClose, HttpServletRequest request
    ) {
    var user = userService.getUser(request).orElse(null);
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
    var newProgram = new Program(programId, title, Year.of(year), semester,
      applicationOpen.atZone(Config.ZONE_ID).toInstant(),
      applicationClose.atZone(Config.ZONE_ID).toInstant(),
      startDate, endDate, facultyLead, description);
    programRepository.save(newProgram);
    return new UpdateProgramInfo.Success();
  }


  public sealed interface GetEditProgramInfo {
    record Success(Program program, Admin admin) implements GetEditProgramInfo {}
    record ProgramNotFound() implements GetEditProgramInfo {}
    record UserNotAdmin() implements GetEditProgramInfo {}
    record NotLoggedIn() implements GetEditProgramInfo {}
  }

  public sealed interface UpdateProgramInfo {
    record Success() implements UpdateProgramInfo {}
    record ProgramNotFound() implements UpdateProgramInfo {}
    record UserNotAdmin() implements UpdateProgramInfo {}
    record NotLoggedIn() implements UpdateProgramInfo {}
  }

}
