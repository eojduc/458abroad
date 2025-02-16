package com.example.abroad.service;


import com.example.abroad.model.Program;
import com.example.abroad.respository.ProgramRepository;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public record ProgramService(ProgramRepository programRepository) {


  public SaveProgram saveProgram(Program program) {
    if (program.title().isBlank()) {
      return new SaveProgram.TitleInvalid();
    }
    if (program.title().length() > 80) {
      return new SaveProgram.TitleInvalid();
    }
    if (program.description().isBlank()) {
      return new SaveProgram.DescriptionInvalid();
    }
    if (program.description().length() > 10000) {
      return new SaveProgram.DescriptionInvalid();
    }
    if (program.applicationOpen().isAfter(program.applicationClose())) {
      return new SaveProgram.ApplicationOpenAfterClose();
    }
    if (program.applicationClose().isAfter(program.documentDeadline())) {
      return new SaveProgram.ApplicationCloseAfterDocumentDeadline();
    }
    if (program.documentDeadline().isAfter(program.startDate())) {
      return new SaveProgram.DocumentDeadlineAfterStart();
    }
    if (program.startDate().isAfter(program.endDate())) {
      return new SaveProgram.StartAfterEnd();
    }
    var savedProgram = programRepository.save(program);
    return new SaveProgram.Success(savedProgram);
  }


  public sealed interface SaveProgram {
    record Success(Program program) implements SaveProgram {}
    record TitleInvalid() implements SaveProgram {}
    record DescriptionInvalid() implements SaveProgram {}
    record ApplicationOpenAfterClose() implements SaveProgram {}
    record ApplicationCloseAfterDocumentDeadline() implements SaveProgram {}
    record DocumentDeadlineAfterStart() implements SaveProgram {}
    record StartAfterEnd() implements SaveProgram {}
  }


  public Optional<Program> findById(Integer id) {
    return programRepository.findById(id);
  }

}
