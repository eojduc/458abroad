package com.example.abroad.service;

import static org.assertj.core.api.Assertions.assertThat;
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
import com.example.abroad.service.AdminProgramInfoService.Column;
import com.example.abroad.service.AdminProgramInfoService.DeleteProgram;
import com.example.abroad.service.AdminProgramInfoService.Filter;
import com.example.abroad.service.AdminProgramInfoService.GetProgramInfo;
import com.example.abroad.service.AdminProgramInfoService.SortApplicantTable;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
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

  // ----------------------------------------------------------
  // Tests for getProgramInfo
  // ----------------------------------------------------------

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
    assertThat(result).isEqualTo(new GetProgramInfo.ProgramNotFound());
  }

  @Test
  public void testGetProgramInfo() {
    when(userService.getUser(request)).thenReturn(Optional.of(TestConstants.ADMIN));
    when(adminRepository.findByUsername(TestConstants.ADMIN.username())).thenReturn(Optional.of(TestConstants.ADMIN));
    when(programRepository.findById(PROGRAM.id())).thenReturn(Optional.of(PROGRAM));
    when(studentRepository.findAll()).thenReturn(List.of(TestConstants.STUDENT));
    when(applicationRepository.findByProgramId(PROGRAM.id())).thenReturn(List.of(TestConstants.APPLICATION));
    var result = service.getProgramInfo(PROGRAM.id(), request);
    assertThat(result).isEqualTo(
      new GetProgramInfo.Success(
        PROGRAM,
        List.of(applicant(TestConstants.STUDENT, TestConstants.APPLICATION)),
        TestConstants.ADMIN
      )
    );
  }

  // ----------------------------------------------------------
  // Tests for deleteProgram
  // ----------------------------------------------------------

  @Test
  public void testDeleteProgramUserNotFound() {
    when(userService.getUser(request)).thenReturn(Optional.empty());
    var result = service.deleteProgram(PROGRAM.id(), request);
    verify(userService).getUser(request);
    assertThat(result).isEqualTo(new DeleteProgram.UserNotFound());
  }

  @Test
  public void testDeleteProgramNotAdmin() {
    when(userService.getUser(request)).thenReturn(Optional.of(TestConstants.STUDENT));
    when(adminRepository.findByUsername(TestConstants.STUDENT.username())).thenReturn(Optional.empty());
    var result = service.deleteProgram(PROGRAM.id(), request);
    assertThat(result).isEqualTo(new DeleteProgram.UserNotAdmin());
  }

  @Test
  public void testDeleteProgramProgramNotFound() {
    when(userService.getUser(request)).thenReturn(Optional.of(TestConstants.ADMIN));
    when(adminRepository.findByUsername(TestConstants.ADMIN.username())).thenReturn(Optional.of(TestConstants.ADMIN));
    when(programRepository.findById(PROGRAM.id())).thenReturn(Optional.empty());
    var result = service.deleteProgram(PROGRAM.id(), request);
    assertThat(result).isEqualTo(new DeleteProgram.ProgramNotFound());
  }

  @Test
  public void testDeleteProgramSuccess() {
    when(userService.getUser(request)).thenReturn(Optional.of(TestConstants.ADMIN));
    when(adminRepository.findByUsername(TestConstants.ADMIN.username())).thenReturn(Optional.of(TestConstants.ADMIN));
    when(programRepository.findById(PROGRAM.id())).thenReturn(Optional.of(PROGRAM));

    var result = service.deleteProgram(PROGRAM.id(), request);

    verify(programRepository).deleteById(PROGRAM.id());
    assertThat(result).isEqualTo(new DeleteProgram.Success());
  }

  // ----------------------------------------------------------
  // Tests for sortApplicantTable
  // ----------------------------------------------------------

  @Test
  public void testSortApplicantTableUserNotFound() {
    // If user is not found, we should get a Failure.
    when(userService.getUser(request)).thenReturn(Optional.empty());

    var result = service.sortApplicantTable(
      Optional.of(Column.USERNAME),
      Optional.of(Filter.APPLIED),
      PROGRAM.id(),
      request
    );

    assertThat(result).isInstanceOf(SortApplicantTable.Failure.class);
  }

  @Test
  public void testSortApplicantTableUserNotAdmin() {
    // User is found but not an admin -> Failure
    when(userService.getUser(request)).thenReturn(Optional.of(TestConstants.STUDENT));
    when(adminRepository.findByUsername(TestConstants.STUDENT.username())).thenReturn(Optional.empty());

    var result = service.sortApplicantTable(
      Optional.of(Column.USERNAME),
      Optional.of(Filter.APPLIED),
      PROGRAM.id(),
      request
    );

    assertThat(result).isInstanceOf(SortApplicantTable.Failure.class);
  }

  @Test
  public void testSortApplicantTableProgramNotFound() {
    // User is admin but program not found -> Failure
    when(userService.getUser(request)).thenReturn(Optional.of(TestConstants.ADMIN));
    when(adminRepository.findByUsername(TestConstants.ADMIN.username())).thenReturn(Optional.of(TestConstants.ADMIN));
    when(programRepository.findById(PROGRAM.id())).thenReturn(Optional.empty());

    var result = service.sortApplicantTable(
      Optional.of(Column.USERNAME),
      Optional.of(Filter.APPLIED),
      PROGRAM.id(),
      request
    );

    assertThat(result).isInstanceOf(SortApplicantTable.Failure.class);
  }

  @Test
  public void testSortApplicantTableSuccess() {
    // User is admin, program is found, let's create multiple Students/Applications
    when(userService.getUser(request)).thenReturn(Optional.of(TestConstants.ADMIN));
    when(adminRepository.findByUsername(TestConstants.ADMIN.username()))
      .thenReturn(Optional.of(TestConstants.ADMIN));
    when(programRepository.findById(PROGRAM.id())).thenReturn(Optional.of(PROGRAM));

    // Define two different Students
    var studentA = TestConstants.STUDENT;
    var studentB = TestConstants.STUDENT_2;

    // Applications with different statuses (e.g. APPLIED, ENROLLED)
    var applicationA = TestConstants.APPLICATION;

    var applicationB = TestConstants.APPLICATION_2;

    // Mock DB calls
    when(studentRepository.findAll()).thenReturn(List.of(studentA, studentB));
    when(applicationRepository.findByProgramId(PROGRAM.id()))
      .thenReturn(List.of(applicationA, applicationB));

    // We'll filter by APPLIED and sort by USERNAME
    var result = service.sortApplicantTable(
      Optional.of(Column.USERNAME),
      Optional.of(Filter.APPLIED),
      PROGRAM.id(),
      request
    );

    // Check the result
    assertThat(result).isInstanceOf(SortApplicantTable.Success.class);
    SortApplicantTable.Success successResult = (SortApplicantTable.Success) result;

    // We expect only the APPLIED applicant(s), sorted by USERNAME
    // But since we only have one APPLIED (alpha), the list is just that one
    assertThat(successResult.applicants().size()).isEqualTo(1);

    Applicant onlyApplicant = successResult.applicants().get(0);
    assertThat(onlyApplicant).isEqualTo(applicant(studentA, applicationA));
  }

  // Helper for getProgramInfo tests
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
