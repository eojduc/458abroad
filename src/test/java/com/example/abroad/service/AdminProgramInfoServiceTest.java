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

import com.example.abroad.TestConstants;
import com.example.abroad.model.Admin;
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
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
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
  public void testGetProgramInfoWorksForTwoApplicants() {
    when(userService.getUser(request)).thenReturn(Optional.of(ADMIN));
    when(adminRepository.findByUsername(ADMIN.username())).thenReturn(Optional.of(ADMIN));
    when(programRepository.findById(PROGRAM.id())).thenReturn(Optional.of(PROGRAM));
    when(studentRepository.findAll()).thenReturn(List.of(STUDENT, STUDENT_2));
    when(applicationRepository.findByProgramId(PROGRAM.id())).thenReturn(List.of(APPLICATION, APPLICATION_2));
    var result = service.getProgramInfo(PROGRAM.id(), request);
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
    when(userService.getUser(request)).thenReturn(Optional.of(STUDENT));
    when(adminRepository.findByUsername(STUDENT.username())).thenReturn(Optional.empty());
    var result = service.getProgramInfo(PROGRAM.id(), request);
    assertThat(result).isEqualTo(new GetProgramInfo.UserNotAdmin());
  }

  @Test
  public void testGetProgramInfoProgramNotFound() {
    when(userService.getUser(request)).thenReturn(Optional.of(ADMIN));
    when(adminRepository.findByUsername(ADMIN.username())).thenReturn(Optional.of(ADMIN));
    when(programRepository.findById(PROGRAM.id())).thenReturn(Optional.empty());
    var result = service.getProgramInfo(PROGRAM.id(), request);
    assertThat(result).isEqualTo(new GetProgramInfo.ProgramNotFound());
  }

  @Test
  public void testGetProgramInfo() {
    when(userService.getUser(request)).thenReturn(Optional.of(ADMIN));
    when(adminRepository.findByUsername(ADMIN.username())).thenReturn(Optional.of(ADMIN));
    when(programRepository.findById(PROGRAM.id())).thenReturn(Optional.of(PROGRAM));
    when(studentRepository.findAll()).thenReturn(List.of(STUDENT));
    when(applicationRepository.findByProgramId(PROGRAM.id())).thenReturn(List.of(APPLICATION));
    var result = service.getProgramInfo(PROGRAM.id(), request);
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
    when(userService.getUser(request)).thenReturn(Optional.empty());
    var result = service.deleteProgram(PROGRAM.id(), request);
    verify(userService).getUser(request);
    assertThat(result).isEqualTo(new DeleteProgram.UserNotFound());
  }

  @Test
  public void testDeleteProgramNotAdmin() {
    when(userService.getUser(request)).thenReturn(Optional.of(STUDENT));
    when(adminRepository.findByUsername(STUDENT.username())).thenReturn(Optional.empty());
    var result = service.deleteProgram(PROGRAM.id(), request);
    assertThat(result).isEqualTo(new DeleteProgram.UserNotAdmin());
  }

  @Test
  public void testDeleteProgramProgramNotFound() {
    when(userService.getUser(request)).thenReturn(Optional.of(ADMIN));
    when(adminRepository.findByUsername(ADMIN.username())).thenReturn(Optional.of(ADMIN));
    when(programRepository.findById(PROGRAM.id())).thenReturn(Optional.empty());
    var result = service.deleteProgram(PROGRAM.id(), request);
    assertThat(result).isEqualTo(new DeleteProgram.ProgramNotFound());
  }

  @Test
  public void testDeleteProgramSuccess() {
    when(userService.getUser(request)).thenReturn(Optional.of(ADMIN));
    when(adminRepository.findByUsername(ADMIN.username())).thenReturn(Optional.of(ADMIN));
    when(programRepository.findById(PROGRAM.id())).thenReturn(Optional.of(PROGRAM));

    var result = service.deleteProgram(PROGRAM.id(), request);

    verify(programRepository).deleteById(PROGRAM.id());
    assertThat(result).isEqualTo(new DeleteProgram.Success());
  }

  @Test
  public void testSortApplicantTableUserNotFound() {
    when(userService.getUser(request)).thenReturn(Optional.empty());

    var result = service.sortApplicantTable(
      Optional.of(Column.USERNAME),
      Optional.of(Filter.NONE),
      PROGRAM.id(),
      request
    );
    assertThat(result).isEqualTo(new SortApplicantTable.Failure());
  }

  @Test
  public void testSortApplicantTableUserNotAdmin() {
    when(userService.getUser(request)).thenReturn(Optional.of(STUDENT));
    when(adminRepository.findByUsername(STUDENT.username())).thenReturn(Optional.empty());

    var result = service.sortApplicantTable(
      Optional.of(Column.USERNAME),
      Optional.of(Filter.APPLIED),
      PROGRAM.id(),
      request
    );
    assertThat(result).isEqualTo(new SortApplicantTable.Failure());
  }

  @Test
  public void testSortApplicantTableProgramNotFound() {
    when(userService.getUser(request)).thenReturn(Optional.of(ADMIN));
    when(adminRepository.findByUsername(ADMIN.username())).thenReturn(Optional.of(ADMIN));
    when(programRepository.findById(PROGRAM.id())).thenReturn(Optional.empty());

    var result = service.sortApplicantTable(
      Optional.of(Column.USERNAME),
      Optional.of(Filter.APPLIED),
      PROGRAM.id(),
      request
    );
    assertThat(result).isEqualTo(new SortApplicantTable.Failure());
  }

  @Test
  public void testSortApplicantTableSuccess() {
    when(userService.getUser(request)).thenReturn(Optional.of(ADMIN));
    when(adminRepository.findByUsername(ADMIN.username()))
      .thenReturn(Optional.of(ADMIN));
    when(programRepository.findById(PROGRAM.id())).thenReturn(Optional.of(PROGRAM));
    when(studentRepository.findAll()).thenReturn(List.of(STUDENT, STUDENT_2, STUDENT_3));
    when(applicationRepository.findByProgramId(PROGRAM.id()))
      .thenReturn(List.of(APPLICATION, APPLICATION_2));

    var result = service.sortApplicantTable(
      Optional.of(Column.USERNAME),
      Optional.of(Filter.NONE),
      PROGRAM.id(),
      request
    );

    assertThat(result).isEqualTo(
      new SortApplicantTable.Success(List.of(applicant(STUDENT, APPLICATION), applicant(STUDENT_2, APPLICATION_2)), PROGRAM
      )
    );
  }

  @Test
  public void testSorApplicantTableBySorters() {

    when(userService.getUser(request)).thenReturn(Optional.of(ADMIN));
    when(adminRepository.findByUsername(ADMIN.username()))
      .thenReturn(Optional.of(ADMIN));
    when(programRepository.findById(PROGRAM.id())).thenReturn(Optional.of(PROGRAM));
    when(studentRepository.findAll()).thenReturn(List.of(STUDENT, STUDENT_2, STUDENT_3));
    when(applicationRepository.findByProgramId(PROGRAM.id()))
      .thenReturn(List.of(APPLICATION, APPLICATION_2, APPLICATION_3));
    var applicants = Stream.of(applicant(STUDENT, APPLICATION), applicant(STUDENT_2, APPLICATION_2), applicant(STUDENT_3, APPLICATION_3)).toList();
    var result1 = service.sortApplicantTable(
      Optional.of(Column.USERNAME),
      Optional.of(Filter.NONE),
      PROGRAM.id(),
      request
    );

    assertThat(result1).isEqualTo(
      new SortApplicantTable.Success(
        applicants.stream()
          .sorted(Comparator.comparing(Applicant::username)).toList(),
        PROGRAM
      )
    );

    var result2 = service.sortApplicantTable(
      Optional.of(Column.DOB),
      Optional.of(Filter.NONE),
      PROGRAM.id(),
      request
    );
    assertThat(result2).isEqualTo(
      new SortApplicantTable.Success(
        applicants.stream().sorted(Comparator.comparing(Applicant::dob)).toList(),
        PROGRAM
      )
    );

    var result3 = service.sortApplicantTable(
      Optional.of(Column.GPA),
      Optional.of(Filter.NONE),
      PROGRAM.id(),
      request
    );

    assertThat(result3).isEqualTo(
      new SortApplicantTable.Success(
        applicants.stream().sorted(Comparator.comparing(Applicant::gpa)).toList(),
        PROGRAM
      )
    );
    var result4 = service.sortApplicantTable(
      Optional.of(Column.STATUS),
      Optional.of(Filter.NONE),
      PROGRAM.id(),
      request
    );
    assertThat(result4).isEqualTo(
      new SortApplicantTable.Success(
        applicants.stream().sorted(Comparator.comparing(Applicant::status)).toList(),
        PROGRAM
      )
    );

    var result5 = service.sortApplicantTable(
      Optional.of(Column.MAJOR),
      Optional.of(Filter.NONE),
      PROGRAM.id(),
      request
    );
    assertThat(result5).isEqualTo(
      new SortApplicantTable.Success(
        applicants.stream().sorted(Comparator.comparing(Applicant::major)).toList(),
        PROGRAM
      )
    );

    var result6 = service.sortApplicantTable(
      Optional.of(Column.EMAIL),
      Optional.of(Filter.NONE),
      PROGRAM.id(),
      request
    );
    assertThat(result6).isEqualTo(
      new SortApplicantTable.Success(
        applicants.stream().sorted(Comparator.comparing(Applicant::email)).toList(),
        PROGRAM
      )
    );

    var result7 = service.sortApplicantTable(
      Optional.of(Column.DISPLAY_NAME),
      Optional.of(Filter.NONE),
      PROGRAM.id(),
      request
    );

    assertThat(result7).isEqualTo(
      new SortApplicantTable.Success(
        applicants.stream().sorted(Comparator.comparing(Applicant::displayName)).toList(),
        PROGRAM
      )
    );

    var result8 = service.sortApplicantTable(
      Optional.of(Column.NONE),
      Optional.of(Filter.NONE),
      PROGRAM.id(),
      request
    );

    assertThat(result8).isEqualTo(
      new SortApplicantTable.Success(
        applicants.stream().sorted(Comparator.comparing(Applicant::username)).toList(),
        PROGRAM
      )
    );

    var result9 = service.sortApplicantTable(
      Optional.empty(),
      Optional.of(Filter.NONE),
      PROGRAM.id(),
      request
    );

    assertThat(result9).isEqualTo(
      new SortApplicantTable.Success(
        applicants.stream().sorted(Comparator.comparing(Applicant::username)).toList(),
        PROGRAM
      )
    );


    var result10 = service.sortApplicantTable(
      Optional.empty(),
      Optional.of(Filter.NONE),
      PROGRAM.id(),
      request
    );

    assertThat(result10).isEqualTo(
      new SortApplicantTable.Success(
        applicants.stream().sorted(Comparator.comparing(Applicant::username)).toList(),
        PROGRAM
      )
    );

    var result11 = service.sortApplicantTable(
      Optional.empty(),
      Optional.of(Filter.APPLIED),
      PROGRAM.id(),
      request
    );

    assertThat(result11).isEqualTo(
      new SortApplicantTable.Success(
        applicants.stream().filter(applicant -> applicant.status().equals(Application.Status.APPLIED)).toList(),
        PROGRAM
      )
    );

    var result12 = service.sortApplicantTable(
      Optional.empty(),
      Optional.of(Filter.ENROLLED),
      PROGRAM.id(),
      request
    );

    assertThat(result12).isEqualTo(
      new SortApplicantTable.Success(
        applicants.stream().filter(applicant -> applicant.status().equals(Application.Status.ENROLLED)).toList(),
        PROGRAM
      )
    );

    var result13 = service.sortApplicantTable(
      Optional.empty(),
      Optional.of(Filter.CANCELLED),
      PROGRAM.id(),
      request
    );

    assertThat(result13).isEqualTo(
      new SortApplicantTable.Success(
        applicants.stream().filter(applicant -> applicant.status().equals(Application.Status.CANCELLED)).toList(),
        PROGRAM
      )
    );

    var result14 = service.sortApplicantTable(
      Optional.empty(),
      Optional.of(Filter.WITHDRAWN),
      PROGRAM.id(),
      request
    );

    assertThat(result14).isEqualTo(
      new SortApplicantTable.Success(
        applicants.stream().filter(applicant -> applicant.status().equals(Application.Status.WITHDRAWN)).toList(),
        PROGRAM
      )
    );

    var result15 = service.sortApplicantTable(
      Optional.empty(),
      Optional.empty(),
      PROGRAM.id(),
      request
    );

    assertThat(result15).isEqualTo(
      new SortApplicantTable.Success(
        applicants.stream().toList(),
        PROGRAM
      )
    );



  }

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
}
