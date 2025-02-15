package com.example.abroad.service;


import com.example.abroad.model.Program;
import com.example.abroad.respository.ProgramRepository;
import org.springframework.stereotype.Service;

@Service
public record ProgramService(ProgramRepository programRepository) {


  public sealed interface SaveProgram {
    record Success(Program program) implements SaveProgram {}
    record DateIncoherence(Field tooEarly, Field tooLate) implements SaveProgram {
      enum Field {
        START_DATE, END_DATE, APPLICATION_DEADLINE, APPLICATION_START_DATE, APPLICATION_END_DATE, DOCUMENT_DEADLINE
      }
    }
  }

}
