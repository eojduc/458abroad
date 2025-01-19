package com.example.abroad.service;

import com.example.abroad.model.Program;
import com.example.abroad.model.Program.Semester;
import com.example.abroad.respository.ApplicationRepository;
import com.example.abroad.respository.ProgramRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Year;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProgramInfoService {
  private final ProgramRepository programRepository;

  private final ApplicationRepository applicationRepository;


  @Autowired
  public ProgramInfoService(ProgramRepository programRepository, ApplicationRepository applicationRepository) {
    this.programRepository = programRepository;
    this.applicationRepository = applicationRepository;
  }

  public record ProgramInfo(
    Program program,
    Integer studentsEnrolled,
    Function<Instant, String> formatInstant,
    Function<LocalDate, String> formatLocalDate,
    Function<Year, String> formatYear,
    Function<Semester, String> formatSemester
  ) {
  }

  public Optional<ProgramInfo> getProgramInfo(String programId) {
    var program = programRepository.findById(programId).orElse(null);
    if (program == null) {
      return Optional.empty();
    }
    var students = applicationRepository.countByProgramId(programId);

    return Optional.of(new ProgramInfo(
      program,
      students,
      (Instant instant) -> instant.atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' hh:mm a")),
      (LocalDate localDate) -> localDate.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy")),
      (Year year) -> year.toString(),
      (Semester semester) -> semester.toString().toLowerCase()
    ));

  }

}
