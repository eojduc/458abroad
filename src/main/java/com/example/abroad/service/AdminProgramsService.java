package com.example.abroad.service;

import com.example.abroad.controller.admin.AdminProgramsController;
import com.example.abroad.model.Application;
import com.example.abroad.model.Program;
import com.example.abroad.model.User;
import com.example.abroad.respository.AdminRepository;
import com.example.abroad.respository.ApplicationRepository;
import com.example.abroad.respository.ProgramRepository;
import com.example.abroad.respository.StudentRepository;
import jakarta.servlet.http.HttpSession;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public record AdminProgramsService(

    ProgramRepository programRepository,
    ApplicationRepository applicationRepository,
    UserService userService,
    StudentRepository studentRepository,
    AdminRepository adminRepository
) {
  private static final Logger logger = LoggerFactory.getLogger(AdminProgramsService.class);

  public GetAllProgramsInfo getProgramInfo(HttpSession session, Optional<String> sort) {
    var user = userService.getUser(session).orElse(null);

    if (user == null) {
      return new GetAllProgramsInfo.UserNotFound();
    }
    if (!user.isAdmin()) {
      return new GetAllProgramsInfo.UserNotAdmin();
    }
    var allPrograms = programRepository.findAll();
    var allApplications = applicationRepository.findAll();

    sort.ifPresent(s -> sortPrograms(session, allPrograms, s));

    return new GetAllProgramsInfo.Success(allPrograms, allApplications, user);
  }
  /**
   * Helper method to handle sorting of the programs based on the given sort parameter.
   */
  private void sortPrograms(HttpSession session, List<Program> programs, String sort) {
    String currentSortDirection = (String) session.getAttribute(sort);
    session.setAttribute(sort, "ascending".equals(currentSortDirection) ? "descending" : "ascending");
    Comparator<Program> comparator = switch (sort) {
      case "title" -> Comparator.comparing(Program::title);
      case "semDate" -> Comparator.comparing(Program::year).thenComparing(Program::semester);
      case "startDate" -> Comparator.comparing(Program::startDate);
      case "endDate" -> Comparator.comparing(Program::endDate);
      case "facultyLead" -> Comparator.comparing(Program::facultyLead);
//      case "totalActive" -> Comparator.comparingInt(Program::totalActive);
      default -> null;
    };

    if (comparator != null && "descending".equals(session.getAttribute(sort))) {
      comparator = comparator.reversed();
    }

    if (comparator != null) {
      programs.sort(comparator);
    }
    logger.info("Sorted programs by {}", sort);
  }
  public sealed interface GetAllProgramsInfo permits
      GetAllProgramsInfo.Success,
      GetAllProgramsInfo.UserNotFound,
      GetAllProgramsInfo.UserNotAdmin {

    record Success(List<Program> programs, List<Application> applicants, User user)
        implements GetAllProgramsInfo { }

    record UserNotFound() implements GetAllProgramsInfo { }
    record UserNotAdmin() implements GetAllProgramsInfo { }
  }



}
