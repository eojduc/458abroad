package com.example.abroad.service;

import com.example.abroad.model.Program;
import com.example.abroad.respository.ApplicationRepository;
import com.example.abroad.respository.ProgramRepository;
import org.springframework.stereotype.Service;

@Service
public class ProgramInfoService {
  private final ProgramRepository programRepository;

  private final ApplicationRepository applicationRepository;


  public ProgramInfoService(ProgramRepository programRepository, ApplicationRepository applicationRepository) {
    this.programRepository = programRepository;
    this.applicationRepository = applicationRepository;
  }

  public sealed interface GetProgramInfoResponse permits ProgramInfo, ProgramNotFound {
  }
  public record ProgramInfo(Program program, Integer studentsEnrolled) implements
    GetProgramInfoResponse {
  }
  public record ProgramNotFound() implements GetProgramInfoResponse { }


  public GetProgramInfoResponse getProgramInfo(String programId) {
    var program = programRepository.findById(programId).orElse(null);
    if (program == null) {
      return new ProgramNotFound();
    }
    var students = applicationRepository.countByProgramId(programId);

    return new ProgramInfo(program, students);
  }

}
