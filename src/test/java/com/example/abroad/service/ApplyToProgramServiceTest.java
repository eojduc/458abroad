package com.example.abroad.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.abroad.TestConstants;
import com.example.abroad.model.Application;
import com.example.abroad.model.Program;
import com.example.abroad.model.Student;
import com.example.abroad.respository.ApplicationRepository;
import com.example.abroad.respository.ProgramRepository;
import com.example.abroad.service.ApplyToProgramService.PageData;
import com.example.abroad.service.ApplyToProgramService.ProgramNotFound;
import com.example.abroad.service.ApplyToProgramService.StudentAlreadyApplied;
import com.example.abroad.service.ApplyToProgramService.SuccessfullyApplied;
import com.example.abroad.service.ApplyToProgramService.UserNotFound;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ApplyToProgramServiceTest {
  private static final Program PROGRAM = TestConstants.PROGRAM;

  private static final Student STUDENT = TestConstants.STUDENT;
  private static final Application APPLICATION = TestConstants.APPLICATION;


  @Mock
  private ApplicationRepository applicationRepository;
  @Mock
  private UserService userService;
  @Mock
  private ProgramRepository programRepository;
  @Mock
  private HttpServletRequest request;

  private ApplyToProgramService service;


  @BeforeEach
  void setUp() {
    service = new ApplyToProgramService(applicationRepository, programRepository, userService);
  }


  @Test
  void testGetPageDataUserNotFound() {
    when(userService.getUser(request)).thenReturn(Optional.empty());

    var response = service.getPageData(PROGRAM.id(), request);
    assertThat(response).isEqualTo(new UserNotFound());
  }


  @Test
  void testGetPageDataProgramNotFound() {
    when(userService.getUser(request)).thenReturn(Optional.of(STUDENT));
    when(programRepository.findById(PROGRAM.id())).thenReturn(Optional.empty());

    var response = service.getPageData(PROGRAM.id(), request);
    assertThat(response).isEqualTo(new ProgramNotFound());
  }


  @Test
  void testGetPageDataStudentAlreadyApplied() {
    when(userService.getUser(request)).thenReturn(Optional.of(STUDENT));
    when(programRepository.findById(PROGRAM.id())).thenReturn(Optional.of(PROGRAM));
    when(applicationRepository.findByProgramIdAndStudent(PROGRAM.id(), STUDENT.username())).thenReturn(Optional.of(APPLICATION));

    var response = service.getPageData(PROGRAM.id(), request);
    assertThat(response).isEqualTo(new StudentAlreadyApplied());
  }

  @Test
  void testGetPageData() {
    when(userService.getUser(request)).thenReturn(Optional.of(STUDENT));
    when(programRepository.findById(PROGRAM.id())).thenReturn(Optional.of(PROGRAM));
    when(applicationRepository.findByProgramIdAndStudent(PROGRAM.id(), STUDENT.username())).thenReturn(Optional.empty());

    var response = service.getPageData(PROGRAM.id(), request);
    assertThat(response).isEqualTo(new PageData(PROGRAM, STUDENT, ApplyToProgramService.QUESTIONS));
  }

  @Test
  void testApplyToProgramUserNotFound() {
    when(userService.getUser(request)).thenReturn(Optional.empty());

    var response = service.applyToProgram(
      APPLICATION.programId(),
      request, APPLICATION.major(), APPLICATION.gpa(), APPLICATION.dateOfBirth(),
      APPLICATION.answer1(), APPLICATION.answer2(), APPLICATION.answer3(),
      APPLICATION.answer4(), APPLICATION.answer5()
    );

    assertThat(response).isEqualTo(new UserNotFound());
  }

  @Test
  void testApplyToProgram() {
    when(userService.getUser(request)).thenReturn(Optional.of(STUDENT));

    var response = service.applyToProgram(
      APPLICATION.programId(),
      request, APPLICATION.major(), APPLICATION.gpa(), APPLICATION.dateOfBirth(),
      APPLICATION.answer1(), APPLICATION.answer2(), APPLICATION.answer3(),
      APPLICATION.answer4(), APPLICATION.answer5()
    );
    verify(applicationRepository).save(new Application(
      APPLICATION.id(), STUDENT.username(), APPLICATION.programId(), APPLICATION.dateOfBirth(),
      APPLICATION.gpa(), APPLICATION.major(), APPLICATION.answer1(), APPLICATION.answer2(),
      APPLICATION.answer3(), APPLICATION.answer4(), APPLICATION.answer5(), Application.Status.APPLIED
    ));
    assertThat(response).isEqualTo(new SuccessfullyApplied());
  }


}
