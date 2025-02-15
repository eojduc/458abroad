package com.example.abroad.service.page;

import com.example.abroad.model.Program;
import com.example.abroad.model.Program.Semester;
import com.example.abroad.model.User;
import com.example.abroad.respository.ProgramRepository;
import com.example.abroad.service.UserService;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.Year;
import org.springframework.stereotype.Service;

@Service
public record AddProgramService(UserService userService, ProgramRepository programRepository) {

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

    // Validate title length
    if (title.length() > 80) {
      return new AddProgramInfo.InvalidProgramInfo("Title must less than 80 characters.");
    }

    // Validate times/dates
    if (!applicationOpen.isBefore(applicationClose)) {
      return new AddProgramInfo.InvalidProgramInfo(
          "Application must open before the application deadline.");
    }
    if (!applicationClose.isBefore(startDate)) {
      return new AddProgramInfo.InvalidProgramInfo(
          "Application deadline must be before the program opens.");
    }
    if (!startDate.isBefore(endDate)) {
      return new AddProgramInfo.InvalidProgramInfo("Program must start before the program end date.");
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

    program = programRepository.saveAndFlush(program);
    return new AddProgramInfo.Success(program.id());
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
