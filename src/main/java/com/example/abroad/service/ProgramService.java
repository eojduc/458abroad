package com.example.abroad.service;


import com.example.abroad.model.Program;
import com.example.abroad.respository.ApplicationRepository;
import com.example.abroad.respository.ProgramRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
/**
 * Service class for Program. wraps the ProgramRepository and provides additional functionality.
 */
@Service
public record ProgramService(ProgramRepository programRepository, ApplicationRepository applicationRepository) {


  public SaveProgram saveProgram(Program program) {
    if (program.title().isBlank()) {
      return new SaveProgram.InvalidProgramInfo("Title cannot be blank");
    }
    if (program.title().length() > 80) {
      return new SaveProgram.InvalidProgramInfo("Title cannot be longer than 80 characters");
    }
    if (program.description().isBlank()) {
      return new SaveProgram.InvalidProgramInfo("Description cannot be blank");
    }
    if (program.description().length() > 10000) {
      return new SaveProgram.InvalidProgramInfo("Description cannot be longer than 10000 characters");
    }
    if (program.applicationOpen().isAfter(program.applicationClose())) {
      return new SaveProgram.InvalidProgramInfo("Application open is after application close");
    }
    if (program.applicationClose().isAfter(program.documentDeadline())) {
      return new SaveProgram.InvalidProgramInfo("Application close is after document deadline");
    }
    if (program.documentDeadline().isAfter(program.startDate())) {
      return new SaveProgram.InvalidProgramInfo("Document deadline is after start date");
    }
    if (program.startDate().isAfter(program.endDate())) {
      return new SaveProgram.InvalidProgramInfo("Start date is after end date");
    }
    try {
      var savedProgram = programRepository.save(program);
      return new SaveProgram.Success(savedProgram);
    } catch (Exception e) {
      return new SaveProgram.DatabaseError(e.getMessage());
    }
  }


  public sealed interface SaveProgram {
    record Success(Program program) implements SaveProgram {}
    record InvalidProgramInfo(String message) implements SaveProgram {}
    record DatabaseError(String message) implements SaveProgram {}
  }


  public Optional<Program> findById(Integer id) {
    return programRepository.findById(id);
  }

  public List<Program> findAll() {
    return programRepository.findAll();
  }


  public List<Program> findAppliedPrograms(String username) {
    var programIds = applicationRepository.findByStudent(username)
        .stream()
        .map(app -> app.programId())
        .toList();
    return programRepository.findAllById(programIds);
  }

}
