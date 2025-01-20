package com.example.abroad.service;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

import com.example.abroad.TestConstants;
import com.example.abroad.model.Application;
import com.example.abroad.model.Program;
import com.example.abroad.model.Student;
import com.example.abroad.respository.ApplicationRepository;
import com.example.abroad.respository.ProgramRepository;
import com.example.abroad.service.ProgramInfoService.GetProgramInfoOutput;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ProgramInfoServiceTest {
  private static final Program PROGRAM = TestConstants.PROGRAM;
  private static final Student STUDENT = TestConstants.STUDENT;
  private static final Application APPLICATION = TestConstants.APPLICATION;

  @Mock
  private ProgramRepository programRepository;
  @Mock
  private UserService userService;
  @Mock
  private ApplicationRepository applicationRepository;
  @Mock
  private HttpServletRequest request;

  private ProgramInfoService service;

  @BeforeEach
  void setUp() {
    service = new ProgramInfoService(programRepository, applicationRepository, userService);
  }


  @Test
  void testUserNotFound() {
    when(userService.getUser(request)).thenReturn(Optional.empty());


    var response = service.getProgramInfo(PROGRAM.id(), request);
    assertThat(response).isEqualTo(new GetProgramInfoOutput.UserNotFound());

  }

  @Test
  void testProgramNotFound() {
    when(userService.getUser(request)).thenReturn(Optional.of(TestConstants.STUDENT));
    when(programRepository.findById(PROGRAM.id())).thenReturn(Optional.empty());

    var response = service.getProgramInfo(PROGRAM.id(), request);
    assertThat(response).isEqualTo(new GetProgramInfoOutput.ProgramNotFound(STUDENT));
  }

  @Test
  void testGetProgramInfoApplied() {
    when(userService.getUser(request)).thenReturn(Optional.of(STUDENT));
    when(programRepository.findById(PROGRAM.id())).thenReturn(Optional.of(PROGRAM));
    when(applicationRepository.countByProgramId(PROGRAM.id())).thenReturn(1);
    when(applicationRepository.findByProgramIdAndStudent(PROGRAM.id(), STUDENT.username()))
      .thenReturn(Optional.of(APPLICATION));


    var response = service.getProgramInfo(PROGRAM.id(), request);
    assertThat(response).isEqualTo(new GetProgramInfoOutput.Success(PROGRAM, 1, APPLICATION.status().name(), STUDENT));
  }

  @Test
  void testGetProgramInfoNotApplied() {
    when(userService.getUser(request)).thenReturn(Optional.of(STUDENT));
    when(programRepository.findById(PROGRAM.id())).thenReturn(Optional.of(PROGRAM));
    when(applicationRepository.countByProgramId(PROGRAM.id())).thenReturn(1);
    when(applicationRepository.findByProgramIdAndStudent(PROGRAM.id(), STUDENT.username()))
      .thenReturn(Optional.empty());

    var response = service.getProgramInfo(PROGRAM.id(), request);
    assertThat(response).isEqualTo(new GetProgramInfoOutput.Success(PROGRAM, 1, "NOT_APPLIED", STUDENT));

  }



}