package com.example.abroad.service;

import static com.example.abroad.TestConstants.ADMIN;
import static com.example.abroad.TestConstants.APPLICATION;
import static com.example.abroad.TestConstants.APPLICATION_2;
import static com.example.abroad.TestConstants.APPLICATION_3;
import static com.example.abroad.TestConstants.PROGRAM;
import static com.example.abroad.TestConstants.STUDENT;
import static com.example.abroad.TestConstants.STUDENT_2;
import static com.example.abroad.TestConstants.STUDENT_3;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.abroad.model.Application;
import com.example.abroad.model.Student;
import com.example.abroad.respository.AdminRepository;
import com.example.abroad.respository.ApplicationRepository;
import com.example.abroad.respository.ProgramRepository;
import com.example.abroad.respository.StudentRepository;
import com.example.abroad.service.admin.AdminProgramInfoService;
import com.example.abroad.service.admin.AdminProgramInfoService.Applicant;
import com.example.abroad.service.admin.AdminProgramInfoService.Column;
import com.example.abroad.service.admin.AdminProgramInfoService.DeleteProgram;
import com.example.abroad.service.admin.AdminProgramInfoService.Filter;
import com.example.abroad.service.admin.AdminProgramInfoService.GetProgramInfo;
import com.example.abroad.service.admin.AdminProgramInfoService.Sort;
import jakarta.servlet.http.HttpSession;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class AdminProgramInfoServiceTest {

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
  @Mock
  private HttpSession session;

  private AdminProgramInfoService service;

  // Helper for getProgramInfo tests
  private static Applicant applicant(Student student, Application application) {
    return new Applicant(
      student.username(),
      student.displayName(),
      student.email(),
      application.major(),
      application.gpa(),
      application.dateOfBirth(),
      application.status(),
      application.id()
    );
  }

  // ----------------------------------------------------------
  // Tests for getProgramInfo (with new fifth parameter for sort order)
  // ----------------------------------------------------------

  @BeforeEach
  public void setUp() {
    service = new AdminProgramInfoService(
      programRepository,
      applicationRepository,
      userService,
      studentRepository,
      adminRepository
    );
  }

  @Test
  public void testGetProgramInfoNotFound() {
    when(userService.getUser(session)).thenReturn(Optional.empty());
    var result = service.getProgramInfo(
      PROGRAM.id(),
      session,
      Optional.empty(),
      Optional.empty(),
      Optional.of(Sort.ASCENDING)
    );
    verify(userService).getUser(session);
    assertThat(result).isEqualTo(new GetProgramInfo.UserNotFound());
  }

  @Test
  public void testGetProgramInfoWorksForTwoApplicants() {
    when(userService.getUser(session)).thenReturn(Optional.of(ADMIN));
    when(adminRepository.findByUsername(ADMIN.username())).thenReturn(Optional.of(ADMIN));
    when(programRepository.findById(PROGRAM.id())).thenReturn(Optional.of(PROGRAM));
    when(studentRepository.findAll()).thenReturn(List.of(STUDENT, STUDENT_2));
    when(applicationRepository.findByProgramId(PROGRAM.id()))
      .thenReturn(List.of(APPLICATION, APPLICATION_2));

    var result = service.getProgramInfo(
      PROGRAM.id(),
      session,
      Optional.empty(),
      Optional.empty(),
      Optional.of(Sort.ASCENDING)
    );
    assertThat(result).isEqualTo(
      new GetProgramInfo.Success(
        PROGRAM,
        List.of(applicant(STUDENT, APPLICATION), applicant(STUDENT_2, APPLICATION_2)),
        ADMIN
      )
    );
  }

  @Test
  public void testGetProgramInfoNotAdmin() {
    when(userService.getUser(session)).thenReturn(Optional.of(STUDENT));
    when(adminRepository.findByUsername(STUDENT.username())).thenReturn(Optional.empty());
    var result = service.getProgramInfo(
      PROGRAM.id(),
      session,
      Optional.empty(),
      Optional.empty(),
      Optional.of(Sort.ASCENDING)
    );
    assertThat(result).isEqualTo(new GetProgramInfo.UserNotAdmin());
  }

  @Test
  public void testGetProgramInfoProgramNotFound() {
    when(userService.getUser(session)).thenReturn(Optional.of(ADMIN));
    when(adminRepository.findByUsername(ADMIN.username())).thenReturn(Optional.of(ADMIN));
    when(programRepository.findById(PROGRAM.id())).thenReturn(Optional.empty());
    var result = service.getProgramInfo(
      PROGRAM.id(),
      session,
      Optional.empty(),
      Optional.empty(),
      Optional.of(Sort.ASCENDING)
    );
    assertThat(result).isEqualTo(new GetProgramInfo.ProgramNotFound());
  }

  @Test
  public void testGetProgramInfoSuccess() {
    when(userService.getUser(session)).thenReturn(Optional.of(ADMIN));
    when(adminRepository.findByUsername(ADMIN.username())).thenReturn(Optional.of(ADMIN));
    when(programRepository.findById(PROGRAM.id())).thenReturn(Optional.of(PROGRAM));
    when(studentRepository.findAll()).thenReturn(List.of(STUDENT));
    when(applicationRepository.findByProgramId(PROGRAM.id())).thenReturn(List.of(APPLICATION));

    var result = service.getProgramInfo(
      PROGRAM.id(),
      session,
      Optional.empty(),
      Optional.empty(),
      Optional.of(Sort.ASCENDING)
    );
    assertThat(result).isEqualTo(
      new GetProgramInfo.Success(
        PROGRAM,
        List.of(applicant(STUDENT, APPLICATION)),
        ADMIN
      )
    );
  }

  // ----------------------------------------------------------
  // Tests for deleteProgram
  // ----------------------------------------------------------

  @Test
  public void testDeleteProgramUserNotFound() {
    when(userService.getUser(session)).thenReturn(Optional.empty());
    var result = service.deleteProgram(PROGRAM.id(), session);
    verify(userService).getUser(session);
    assertThat(result).isEqualTo(new DeleteProgram.UserNotFound());
  }

  @Test
  public void testDeleteProgramNotAdmin() {
    when(userService.getUser(session)).thenReturn(Optional.of(STUDENT));
    when(adminRepository.findByUsername(STUDENT.username())).thenReturn(Optional.empty());
    var result = service.deleteProgram(PROGRAM.id(), session);
    assertThat(result).isEqualTo(new DeleteProgram.UserNotAdmin());
  }

  @Test
  public void testDeleteProgramProgramNotFound() {
    when(userService.getUser(session)).thenReturn(Optional.of(ADMIN));
    when(adminRepository.findByUsername(ADMIN.username())).thenReturn(Optional.of(ADMIN));
    when(programRepository.findById(PROGRAM.id())).thenReturn(Optional.empty());
    var result = service.deleteProgram(PROGRAM.id(), session);
    assertThat(result).isEqualTo(new DeleteProgram.ProgramNotFound());
  }

  @Test
  public void testDeleteProgramSuccess() {
    when(userService.getUser(session)).thenReturn(Optional.of(ADMIN));
    when(adminRepository.findByUsername(ADMIN.username())).thenReturn(Optional.of(ADMIN));
    when(programRepository.findById(PROGRAM.id())).thenReturn(Optional.of(PROGRAM));

    var result = service.deleteProgram(PROGRAM.id(), session);
    verify(programRepository).deleteById(PROGRAM.id());
    assertThat(result).isEqualTo(new DeleteProgram.Success());
  }

  // ----------------------------------------------------------
  // Tests for sorting and filtering (formerly using sortApplicantTable)
  // ----------------------------------------------------------

  @Test
  public void testSortApplicantTableUserNotFound() {
    when(userService.getUser(session)).thenReturn(Optional.empty());

    var result = service.getProgramInfo(
      PROGRAM.id(),
      session,
      Optional.of(Column.USERNAME),
      Optional.of(Filter.NONE),
      Optional.of(Sort.ASCENDING)
    );
    assertThat(result).isEqualTo(new GetProgramInfo.UserNotFound());
  }

  @Test
  public void testSortApplicantTableUserNotAdmin() {
    when(userService.getUser(session)).thenReturn(Optional.of(STUDENT));
    when(adminRepository.findByUsername(STUDENT.username())).thenReturn(Optional.empty());

    var result = service.getProgramInfo(
      PROGRAM.id(),
      session,
      Optional.of(Column.USERNAME),
      Optional.of(Filter.APPLIED),
      Optional.of(Sort.ASCENDING)
    );
    assertThat(result).isEqualTo(new GetProgramInfo.UserNotAdmin());
  }

  @Test
  public void testSortApplicantTableProgramNotFound() {
    when(userService.getUser(session)).thenReturn(Optional.of(ADMIN));
    when(adminRepository.findByUsername(ADMIN.username())).thenReturn(Optional.of(ADMIN));
    when(programRepository.findById(PROGRAM.id())).thenReturn(Optional.empty());

    var result = service.getProgramInfo(
      PROGRAM.id(),
      session,
      Optional.of(Column.USERNAME),
      Optional.of(Filter.APPLIED),
      Optional.of(Sort.ASCENDING)
    );
    assertThat(result).isEqualTo(new GetProgramInfo.ProgramNotFound());
  }

  @Test
  public void testSortApplicantTableSuccess() {
    when(userService.getUser(session)).thenReturn(Optional.of(ADMIN));
    when(adminRepository.findByUsername(ADMIN.username())).thenReturn(Optional.of(ADMIN));
    when(programRepository.findById(PROGRAM.id())).thenReturn(Optional.of(PROGRAM));
    // Three students but only two applications exist
    when(studentRepository.findAll()).thenReturn(List.of(STUDENT, STUDENT_2, STUDENT_3));
    when(applicationRepository.findByProgramId(PROGRAM.id()))
      .thenReturn(List.of(APPLICATION, APPLICATION_2));

    var result = service.getProgramInfo(
      PROGRAM.id(),
      session,
      Optional.of(Column.USERNAME),
      Optional.of(Filter.NONE),
      Optional.of(Sort.ASCENDING)
    );

    assertThat(result).isEqualTo(
      new GetProgramInfo.Success(
        PROGRAM,
        List.of(applicant(STUDENT, APPLICATION), applicant(STUDENT_2, APPLICATION_2)),
        ADMIN
      )
    );
  }

  @Test
  public void testSortApplicantTableBySorters() {
    when(userService.getUser(session)).thenReturn(Optional.of(ADMIN));
    when(adminRepository.findByUsername(ADMIN.username())).thenReturn(Optional.of(ADMIN));
    when(programRepository.findById(PROGRAM.id())).thenReturn(Optional.of(PROGRAM));
    when(studentRepository.findAll()).thenReturn(List.of(STUDENT, STUDENT_2, STUDENT_3));
    when(applicationRepository.findByProgramId(PROGRAM.id()))
      .thenReturn(List.of(APPLICATION, APPLICATION_2, APPLICATION_3));

    var applicants = Stream.of(
      applicant(STUDENT, APPLICATION),
      applicant(STUDENT_2, APPLICATION_2),
      applicant(STUDENT_3, APPLICATION_3)
    ).toList();

    var result1 = service.getProgramInfo(
      PROGRAM.id(),
      session,
      Optional.of(Column.USERNAME),
      Optional.of(Filter.NONE),
      Optional.of(Sort.ASCENDING)
    );
    assertThat(result1).isEqualTo(
      new GetProgramInfo.Success(
        PROGRAM,
        applicants.stream()
          .sorted(Comparator.comparing(Applicant::username))
          .toList(),
        ADMIN
      )
    );

    var result2 = service.getProgramInfo(
      PROGRAM.id(),
      session,
      Optional.of(Column.DOB),
      Optional.of(Filter.NONE),
      Optional.of(Sort.ASCENDING)
    );
    assertThat(result2).isEqualTo(
      new GetProgramInfo.Success(
        PROGRAM,
        applicants.stream().sorted(Comparator.comparing(Applicant::dob)).toList(),
        ADMIN
      )
    );

    var result3 = service.getProgramInfo(
      PROGRAM.id(),
      session,
      Optional.of(Column.GPA),
      Optional.of(Filter.NONE),
      Optional.of(Sort.ASCENDING)
    );
    assertThat(result3).isEqualTo(
      new GetProgramInfo.Success(
        PROGRAM,
        applicants.stream().sorted(Comparator.comparing(Applicant::gpa)).toList(),
        ADMIN
      )
    );

    var result4 = service.getProgramInfo(
      PROGRAM.id(),
      session,
      Optional.of(Column.STATUS),
      Optional.of(Filter.NONE),
      Optional.of(Sort.ASCENDING)
    );
    assertThat(result4).isEqualTo(
      new GetProgramInfo.Success(
        PROGRAM,
        applicants.stream().sorted(Comparator.comparing(Applicant::status)).toList(),
        ADMIN
      )
    );

    var result5 = service.getProgramInfo(
      PROGRAM.id(),
      session,
      Optional.of(Column.MAJOR),
      Optional.of(Filter.NONE),
      Optional.of(Sort.ASCENDING)
    );
    assertThat(result5).isEqualTo(
      new GetProgramInfo.Success(
        PROGRAM,
        applicants.stream().sorted(Comparator.comparing(Applicant::major)).toList(),
        ADMIN
      )
    );

    var result6 = service.getProgramInfo(
      PROGRAM.id(),
      session,
      Optional.of(Column.EMAIL),
      Optional.of(Filter.NONE),
      Optional.of(Sort.ASCENDING)
    );
    assertThat(result6).isEqualTo(
      new GetProgramInfo.Success(
        PROGRAM,
        applicants.stream().sorted(Comparator.comparing(Applicant::email)).toList(),
        ADMIN
      )
    );

    var result7 = service.getProgramInfo(
      PROGRAM.id(),
      session,
      Optional.of(Column.DISPLAY_NAME),
      Optional.of(Filter.NONE),
      Optional.of(Sort.ASCENDING)
    );
    assertThat(result7).isEqualTo(
      new GetProgramInfo.Success(
        PROGRAM,
        applicants.stream().sorted(Comparator.comparing(Applicant::displayName)).toList(),
        ADMIN
      )
    );

    // When no sort column is provided, default to sorting by username.
    var result8 = service.getProgramInfo(
      PROGRAM.id(),
      session,
      Optional.of(Column.NONE),
      Optional.of(Filter.NONE),
      Optional.of(Sort.ASCENDING)
    );
    assertThat(result8).isEqualTo(
      new GetProgramInfo.Success(
        PROGRAM,
        applicants.stream().sorted(Comparator.comparing(Applicant::username)).toList(),
        ADMIN
      )
    );

    var result9 = service.getProgramInfo(
      PROGRAM.id(),
      session,
      Optional.empty(),
      Optional.of(Filter.NONE),
      Optional.of(Sort.ASCENDING)
    );
    assertThat(result9).isEqualTo(
      new GetProgramInfo.Success(
        PROGRAM,
        applicants.stream().sorted(Comparator.comparing(Applicant::username)).toList(),
        ADMIN
      )
    );

    // Repeating the same call for consistency.
    var result10 = service.getProgramInfo(
      PROGRAM.id(),
      session,
      Optional.empty(),
      Optional.of(Filter.NONE),
      Optional.of(Sort.ASCENDING)
    );
    assertThat(result10).isEqualTo(
      new GetProgramInfo.Success(
        PROGRAM,
        applicants.stream().sorted(Comparator.comparing(Applicant::username)).toList(),
        ADMIN
      )
    );

    var result11 = service.getProgramInfo(
      PROGRAM.id(),
      session,
      Optional.empty(),
      Optional.of(Filter.APPLIED),
      Optional.of(Sort.ASCENDING)
    );
    assertThat(result11).isEqualTo(
      new GetProgramInfo.Success(
        PROGRAM,
        applicants.stream()
          .filter(app -> app.status().equals(Application.Status.APPLIED))
          .toList(),
        ADMIN
      )
    );

    var result12 = service.getProgramInfo(
      PROGRAM.id(),
      session,
      Optional.empty(),
      Optional.of(Filter.ENROLLED),
      Optional.of(Sort.ASCENDING)
    );
    assertThat(result12).isEqualTo(
      new GetProgramInfo.Success(
        PROGRAM,
        applicants.stream()
          .filter(app -> app.status().equals(Application.Status.ENROLLED))
          .toList(),
        ADMIN
      )
    );

    var result13 = service.getProgramInfo(
      PROGRAM.id(),
      session,
      Optional.empty(),
      Optional.of(Filter.CANCELLED),
      Optional.of(Sort.ASCENDING)
    );
    assertThat(result13).isEqualTo(
      new GetProgramInfo.Success(
        PROGRAM,
        applicants.stream()
          .filter(app -> app.status().equals(Application.Status.CANCELLED))
          .toList(),
        ADMIN
      )
    );

    var result14 = service.getProgramInfo(
      PROGRAM.id(),
      session,
      Optional.empty(),
      Optional.of(Filter.WITHDRAWN),
      Optional.of(Sort.ASCENDING)
    );
    assertThat(result14).isEqualTo(
      new GetProgramInfo.Success(
        PROGRAM,
        applicants.stream()
          .filter(app -> app.status().equals(Application.Status.WITHDRAWN))
          .toList(),
        ADMIN
      )
    );

    // With no sort column and Filter.NONE, expect the unsorted list.
    var result15 = service.getProgramInfo(
      PROGRAM.id(),
      session,
      Optional.empty(),
      Optional.of(Filter.NONE),
      Optional.of(Sort.ASCENDING)
    );
    assertThat(result15).isEqualTo(
      new GetProgramInfo.Success(
        PROGRAM,
        applicants,
        ADMIN
      )
    );
  }
}
