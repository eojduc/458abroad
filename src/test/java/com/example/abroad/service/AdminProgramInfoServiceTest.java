package com.example.abroad.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.abroad.TestConstants;
import com.example.abroad.model.Application;
import com.example.abroad.model.Program;
import com.example.abroad.model.Student;
import com.example.abroad.respository.AdminRepository;
import com.example.abroad.respository.ApplicationRepository;
import com.example.abroad.respository.ProgramRepository;
import com.example.abroad.respository.StudentRepository;
import com.example.abroad.service.AdminProgramInfoService.Applicant;
import com.example.abroad.service.AdminProgramInfoService.DeleteProgram;
import com.example.abroad.service.AdminProgramInfoService.GetProgramInfo;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class AdminProgramInfoServiceTest {
  private static final Program PROGRAM = TestConstants.PROGRAM;

  @Mock
  private ProgramRepository programRepository;
  @Mock
  private ApplicationRepository applicationRepository;
  @Mock
  private UserService userService;
  @Mock
  private StudentRepository studentRepository;
  @Mock
  private AdminRepository adminRepository;

  private HttpServletRequest request;


  private AdminProgramInfoService service;

  @BeforeEach
  public void setUp() {
    service = new AdminProgramInfoService(programRepository, applicationRepository, userService, studentRepository, adminRepository);
  }

  @Test
  public void testGetProgramInfoNotFound() {
    when(userService.getUser(request)).thenReturn(Optional.empty());
    var result = service.getProgramInfo(PROGRAM.id(), request);
    verify(userService).getUser(request);
    assertThat(result).isEqualTo(new GetProgramInfo.UserNotFound());
  }

  @Test
  public void testGetProgramInfoNotAdmin() {
    when(userService.getUser(request)).thenReturn(Optional.of(TestConstants.STUDENT));
    when(adminRepository.findByUsername(TestConstants.STUDENT.username())).thenReturn(Optional.empty());
    var result = service.getProgramInfo(PROGRAM.id(), request);
    assertThat(result).isEqualTo(new GetProgramInfo.UserNotAdmin());
  }

  @Test
  public void testGetProgramInfoProgramNotFound() {
    when(userService.getUser(request)).thenReturn(Optional.of(TestConstants.ADMIN));
    when(adminRepository.findByUsername(TestConstants.ADMIN.username())).thenReturn(Optional.of(TestConstants.ADMIN));
    when(programRepository.findById(PROGRAM.id())).thenReturn(Optional.empty());
    var result = service.getProgramInfo(PROGRAM.id(), request);
    assertThat(result).isEqualTo(new GetProgramInfo.ProgramNotFound(TestConstants.ADMIN));
  }

  @Test
  public void testGetProgramInfo() {
    when(userService.getUser(request)).thenReturn(Optional.of(TestConstants.ADMIN));
    when(adminRepository.findByUsername(TestConstants.ADMIN.username())).thenReturn(Optional.of(TestConstants.ADMIN));
    when(programRepository.findById(PROGRAM.id())).thenReturn(Optional.of(PROGRAM));
    when(studentRepository.findAll()).thenReturn(List.of(TestConstants.STUDENT));
    when(applicationRepository.findByProgramId(PROGRAM.id())).thenReturn(List.of(TestConstants.APPLICATION));
    var result = service.getProgramInfo(PROGRAM.id(), request);
    assertThat(result).isEqualTo(new GetProgramInfo.Success(
      PROGRAM, List.of(applicant(TestConstants.STUDENT, TestConstants.APPLICATION)), TestConstants.ADMIN
    ));
  }

  @Test
  public void testDeleteProgramUserNotFound() {
    // userService returns an empty Optional, so user is not found
    when(userService.getUser(request)).thenReturn(Optional.empty());

    var result = service.deleteProgram(PROGRAM.id(), request);

    verify(userService).getUser(request);
    assertThat(result).isEqualTo(new DeleteProgram.UserNotFound());
  }

  @Test
  public void testDeleteProgramNotAdmin() {
    // userService finds a user, but it's not an admin
    when(userService.getUser(request)).thenReturn(Optional.of(TestConstants.STUDENT));
    when(adminRepository.findByUsername(TestConstants.STUDENT.username()))
      .thenReturn(Optional.empty());

    var result = service.deleteProgram(PROGRAM.id(), request);

    assertThat(result).isEqualTo(new DeleteProgram.UserNotAdmin());
  }

  @Test
  public void testDeleteProgramProgramNotFound() {
    // user is an admin, but the program doesn't exist
    when(userService.getUser(request)).thenReturn(Optional.of(TestConstants.ADMIN));
    when(adminRepository.findByUsername(TestConstants.ADMIN.username()))
      .thenReturn(Optional.of(TestConstants.ADMIN));
    when(programRepository.findById(PROGRAM.id())).thenReturn(Optional.empty());

    var result = service.deleteProgram(PROGRAM.id(), request);

    assertThat(result).isEqualTo(new DeleteProgram.ProgramNotFound());
  }

  @Test
  public void testDeleteProgramSuccess() {
    // user is an admin and the program is found
    when(userService.getUser(request)).thenReturn(Optional.of(TestConstants.ADMIN));
    when(adminRepository.findByUsername(TestConstants.ADMIN.username()))
      .thenReturn(Optional.of(TestConstants.ADMIN));
    when(programRepository.findById(PROGRAM.id())).thenReturn(Optional.of(PROGRAM));

    var result = service.deleteProgram(PROGRAM.id(), request);

    // Ensure the repository is told to delete the program by ID
    verify(programRepository).deleteById(PROGRAM.id());
    assertThat(result).isEqualTo(new DeleteProgram.Success());
  }

  private Applicant applicant(Student student, Application application) {
    return new Applicant(
      student.username(),
      student.displayName(),
      student.email(),
      application.major(),
      application.gpa(),
      application.dateOfBirth(),
      application.status()
    );
  }






}
