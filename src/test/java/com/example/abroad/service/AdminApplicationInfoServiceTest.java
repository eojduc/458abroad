package com.example.abroad.service;

import static com.example.abroad.TestConstants.ADMIN;
import static com.example.abroad.TestConstants.APPLICATION;
import static com.example.abroad.TestConstants.PROGRAM;
import static com.example.abroad.TestConstants.STUDENT;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.abroad.model.Application.Status;
import com.example.abroad.respository.ApplicationRepository;
import com.example.abroad.respository.ProgramRepository;
import com.example.abroad.respository.StudentRepository;
import jakarta.servlet.http.HttpSession;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AdminApplicationInfoServiceTest {

  @Mock
  private FormatService formatService;
  @Mock
  private UserService userService;
  @Mock
  private ProgramRepository programRepository;
  @Mock
  private ApplicationRepository applicationRepository;
  @Mock
  private StudentRepository studentRepository;
  @Mock
  private HttpSession session;

  private AdminApplicationInfoService service;

  // Sample objects for testing

  @BeforeEach
  void setUp() {
    service = new AdminApplicationInfoService(
      formatService,
      userService,
      programRepository,
      applicationRepository,
      studentRepository
    );
  }

  // ---------------------------------------------------------
  // Tests for getApplicationInfo
  // ---------------------------------------------------------

  @Test
  void testGetApplicationInfoNotLoggedIn() {
    // userService returns no user => NotLoggedIn
    when(userService.getUser(session)).thenReturn(Optional.empty());

    var result = service.getApplicationInfo(APPLICATION.id(), session);

    assertThat(result).isEqualTo(new AdminApplicationInfoService.GetApplicationInfo.NotLoggedIn());
  }

  @Test
  void testGetApplicationInfoUserNotAdmin() {
    // userService returns a valid user, but it's not an Admin
    when(userService.getUser(session)).thenReturn(Optional.of(STUDENT));

    var result = service.getApplicationInfo(APPLICATION.id(), session);

    assertThat(result).isEqualTo(new AdminApplicationInfoService.GetApplicationInfo.UserNotAdmin());
  }

  @Test
  void testGetApplicationInfoApplicationNotFound() {
    // user is Admin but application doesn't exist
    when(userService.getUser(session)).thenReturn(Optional.of(ADMIN));
    when(applicationRepository.findById(APPLICATION.id())).thenReturn(Optional.empty());

    var result = service.getApplicationInfo(APPLICATION.id(), session);

    assertThat(result).isEqualTo(
      new AdminApplicationInfoService.GetApplicationInfo.ApplicationNotFound());
  }

  @Test
  void testGetApplicationInfoProgramNotFound() {
    // user is Admin, application is found, but program is missing => ApplicationNotFound
    when(userService.getUser(session)).thenReturn(Optional.of(ADMIN));
    when(applicationRepository.findById(APPLICATION.id())).thenReturn(Optional.of(APPLICATION));
    when(programRepository.findById(PROGRAM.id())).thenReturn(Optional.empty());

    var result = service.getApplicationInfo(APPLICATION.id(), session);

    assertThat(result).isEqualTo(
      new AdminApplicationInfoService.GetApplicationInfo.ApplicationNotFound());
  }

  @Test
  void testGetApplicationInfoStudentNotFound() {
    // user is Admin, application + program found, but student is missing => ApplicationNotFound
    when(userService.getUser(session)).thenReturn(Optional.of(ADMIN));
    when(applicationRepository.findById(APPLICATION.id())).thenReturn(Optional.of(APPLICATION));
    when(programRepository.findById(PROGRAM.id())).thenReturn(Optional.of(PROGRAM));
    when(studentRepository.findByUsername(STUDENT.username())).thenReturn(Optional.empty());

    var result = service.getApplicationInfo(APPLICATION.id(), session);

    assertThat(result).isEqualTo(
      new AdminApplicationInfoService.GetApplicationInfo.ApplicationNotFound());
  }

  @Test
  void testGetApplicationInfoSuccess() {
    // user is Admin, everything is found => Success
    when(userService.getUser(session)).thenReturn(Optional.of(ADMIN));
    when(applicationRepository.findById(APPLICATION.id())).thenReturn(Optional.of(APPLICATION));
    when(programRepository.findById(PROGRAM.id())).thenReturn(Optional.of(PROGRAM));
    when(studentRepository.findByUsername(STUDENT.username())).thenReturn(Optional.of(STUDENT));

    var result = service.getApplicationInfo(APPLICATION.id(), session);

    assertThat(result).isEqualTo(
      new AdminApplicationInfoService.GetApplicationInfo.Success(PROGRAM, STUDENT, APPLICATION,
        ADMIN)
    );
  }

  // ---------------------------------------------------------
  // Tests for updateApplicationStatus
  // ---------------------------------------------------------

  @Test
  void testUpdateApplicationStatusNotLoggedIn() {
    // userService returns no user => NotLoggedIn
    when(userService.getUser(session)).thenReturn(Optional.empty());

    var result = service.updateApplicationStatus(APPLICATION.id(), Status.APPLIED, session);

    assertThat(result).isEqualTo(
      new AdminApplicationInfoService.UpdateApplicationStatus.NotLoggedIn());
  }

  @Test
  void testUpdateApplicationStatusUserNotAdmin() {
    // userService returns a user who is not an Admin
    when(userService.getUser(session)).thenReturn(Optional.of(STUDENT));

    var result = service.updateApplicationStatus(APPLICATION.id(), Status.APPLIED, session);

    assertThat(result).isEqualTo(
      new AdminApplicationInfoService.UpdateApplicationStatus.UserNotAdmin());
  }

  @Test
  void testUpdateApplicationStatusApplicationNotFound() {
    // user is Admin, but the application doesn't exist in the repo
    when(userService.getUser(session)).thenReturn(Optional.of(ADMIN));
    when(applicationRepository.findById(APPLICATION.id())).thenReturn(Optional.empty());

    var result = service.updateApplicationStatus(APPLICATION.id(), Status.APPLIED, session);

    assertThat(result).isEqualTo(
      new AdminApplicationInfoService.UpdateApplicationStatus.ApplicationNotFound());
  }

  @Test
  void testUpdateApplicationStatusSuccess() {
    // user is Admin, application is found => success
    when(userService.getUser(session)).thenReturn(Optional.of(ADMIN));
    when(applicationRepository.findById(APPLICATION.id())).thenReturn(Optional.of(APPLICATION));

    var result = service.updateApplicationStatus(APPLICATION.id(), Status.APPLIED, session);

    // Verify the repo call to update status
    verify(applicationRepository).updateStatus(APPLICATION.id(), Status.APPLIED);
    assertThat(result).isEqualTo(
      new AdminApplicationInfoService.UpdateApplicationStatus.Success(Status.APPLIED)
    );
  }

}
