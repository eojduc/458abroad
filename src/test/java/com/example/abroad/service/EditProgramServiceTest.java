package com.example.abroad.service;

import static com.example.abroad.TestConstants.ADMIN;
import static com.example.abroad.TestConstants.PROGRAM;
import static com.example.abroad.TestConstants.STUDENT;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.abroad.Config;
import com.example.abroad.model.Program;
import com.example.abroad.model.Program.Semester;
import com.example.abroad.respository.ProgramRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class EditProgramServiceTest {

  @Mock
  private UserService userService;
  @Mock
  private ProgramRepository programRepository;
  @Mock
  private HttpServletRequest request;

  private EditProgramService service;

  @BeforeEach
  void setUp() {
    service = new EditProgramService(userService, programRepository);
  }

  // ---------------------------------------------------------
  // Tests for getEditProgramInfo
  // ---------------------------------------------------------

  @Test
  void testGetEditProgramInfoNotLoggedIn() {
    when(userService.getUser(request)).thenReturn(Optional.empty());

    var result = service.getEditProgramInfo(PROGRAM.id(), request);

    assertThat(result).isEqualTo(new EditProgramService.GetEditProgramInfo.NotLoggedIn());
  }

  @Test
  void testGetEditProgramInfoUserNotAdmin() {
    when(userService.getUser(request)).thenReturn(Optional.of(STUDENT));

    var result = service.getEditProgramInfo(PROGRAM.id(), request);

    assertThat(result).isEqualTo(new EditProgramService.GetEditProgramInfo.UserNotAdmin());
  }

  @Test
  void testGetEditProgramInfoProgramNotFound() {
    when(userService.getUser(request)).thenReturn(Optional.of(ADMIN));
    when(programRepository.findById(PROGRAM.id())).thenReturn(Optional.empty());

    var result = service.getEditProgramInfo(PROGRAM.id(), request);

    assertThat(result).isEqualTo(new EditProgramService.GetEditProgramInfo.ProgramNotFound());
  }

  @Test
  void testGetEditProgramInfoSuccess() {
    when(userService.getUser(request)).thenReturn(Optional.of(ADMIN));
    when(programRepository.findById(PROGRAM.id())).thenReturn(Optional.of(PROGRAM));

    var result = service.getEditProgramInfo(PROGRAM.id(), request);

    assertThat(result).isEqualTo(new EditProgramService.GetEditProgramInfo.Success(PROGRAM, ADMIN));
  }

  // ---------------------------------------------------------
  // Tests for updateProgramInfo
  // ---------------------------------------------------------

  @Test
  void testUpdateProgramInfoNotLoggedIn() {
    when(userService.getUser(request)).thenReturn(Optional.empty());

    var result = service.updateProgramInfo(PROGRAM.id(), "New Title", "New Description",
      Year.now().getValue(), LocalDate.now(), LocalDate.now().plusDays(30),
      "New Faculty", Semester.FALL, LocalDateTime.now(), LocalDateTime.now().plusDays(10), request);

    assertThat(result).isEqualTo(new EditProgramService.UpdateProgramInfo.NotLoggedIn());
  }

  @Test
  void testUpdateProgramInfoUserNotAdmin() {
    when(userService.getUser(request)).thenReturn(Optional.of(STUDENT));

    var result = service.updateProgramInfo(PROGRAM.id(), "New Title", "New Description",
      Year.now().getValue(), LocalDate.now(), LocalDate.now().plusDays(30),
      "New Faculty", Semester.FALL, LocalDateTime.now(), LocalDateTime.now().plusDays(10), request);

    assertThat(result).isEqualTo(new EditProgramService.UpdateProgramInfo.UserNotAdmin());
  }

  @Test
  void testUpdateProgramInfoProgramNotFound() {
    when(userService.getUser(request)).thenReturn(Optional.of(ADMIN));
    when(programRepository.findById(PROGRAM.id())).thenReturn(Optional.empty());

    var result = service.updateProgramInfo(PROGRAM.id(), "New Title", "New Description",
      Year.now().getValue(), LocalDate.now(), LocalDate.now().plusDays(30),
      "New Faculty", Semester.FALL, LocalDateTime.now(), LocalDateTime.now().plusDays(10), request);

    assertThat(result).isEqualTo(new EditProgramService.UpdateProgramInfo.ProgramNotFound());
  }

  @Test
  void testUpdateProgramInfoSuccess() {
    when(userService.getUser(request)).thenReturn(Optional.of(ADMIN));
    when(programRepository.findById(PROGRAM.id())).thenReturn(Optional.of(PROGRAM));
    var open = LocalDateTime.now();
    var result = service.updateProgramInfo(PROGRAM.id(), "New Title", "New Description",
      Year.now().getValue(), LocalDate.now(), LocalDate.now().plusDays(30),
      "New Faculty", Semester.FALL, open, open.plusDays(10), request);
    verify(programRepository).save(new Program(PROGRAM.id(), "New Title", Year.now(), Semester.FALL,
      open.atZone(Config.ZONE_ID).toInstant(),
      open.plusDays(10).atZone(Config.ZONE_ID).toInstant(),
      LocalDate.now(), LocalDate.now().plusDays(30), "New Faculty", "New Description"));

    assertThat(result).isEqualTo(new EditProgramService.UpdateProgramInfo.Success());
  }
}
