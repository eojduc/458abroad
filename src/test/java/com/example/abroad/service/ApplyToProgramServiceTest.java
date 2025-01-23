package com.example.abroad.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.abroad.TestConstants;
import com.example.abroad.model.Application;
import com.example.abroad.model.Program;
import com.example.abroad.model.Question;
import com.example.abroad.model.Student;
import com.example.abroad.respository.ApplicationRepository;
import com.example.abroad.respository.ProgramRepository;
import com.example.abroad.service.ApplyToProgramService.ApplyToProgram;
import com.example.abroad.service.ApplyToProgramService.GetApplyPageData;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
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
    assertThat(response).isEqualTo(new GetApplyPageData.UserNotFound());
  }


  @Test
  void testGetPageDataProgramNotFound() {
    when(userService.getUser(request)).thenReturn(Optional.of(STUDENT));
    when(programRepository.findById(PROGRAM.id())).thenReturn(Optional.empty());

    var response = service.getPageData(PROGRAM.id(), request);
    assertThat(response).isEqualTo(new GetApplyPageData.ProgramNotFound());
  }

  @Test
  void testGetPageDataUserNotStudent() {
    when(userService.getUser(request)).thenReturn(Optional.of(TestConstants.ADMIN));

    var response = service.getPageData(PROGRAM.id(), request);
    assertThat(response).isEqualTo(new GetApplyPageData.UserNotStudent());
  }


  @Test
  void testGetPageDataStudentAlreadyApplied() {
    when(userService.getUser(request)).thenReturn(Optional.of(STUDENT));
    when(programRepository.findById(PROGRAM.id())).thenReturn(Optional.of(PROGRAM));
    when(applicationRepository.findByProgramIdAndStudent(PROGRAM.id(), STUDENT.username())).thenReturn(Optional.of(APPLICATION));

    var response = service.getPageData(PROGRAM.id(), request);
    assertThat(response).isEqualTo(new GetApplyPageData.StudentAlreadyApplied(APPLICATION.id()));
  }

  @Test
  void testGetPageData() {
    when(userService.getUser(request)).thenReturn(Optional.of(STUDENT));
    when(programRepository.findById(PROGRAM.id())).thenReturn(Optional.of(PROGRAM));
    when(applicationRepository.findByProgramIdAndStudent(PROGRAM.id(), STUDENT.username())).thenReturn(Optional.empty());

    var response = service.getPageData(PROGRAM.id(), request);
    var maxDayOfBirth = LocalDate.now().minusYears(10).toString();
    assertThat(response).isEqualTo(new GetApplyPageData.Success(PROGRAM, STUDENT, Question.QUESTIONS, maxDayOfBirth));
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

    assertThat(response).isEqualTo(new ApplyToProgram.UserNotFound());
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
      APPLICATION.gpa(), APPLICATION.major(), APPLICATION.answer1(),
      APPLICATION.answer2(), APPLICATION.answer3(), APPLICATION.answer4(), APPLICATION.answer5(), Application.Status.APPLIED
    ));
    assertThat(response).isEqualTo(new ApplyToProgram.Success(APPLICATION.id()));
  }


}
