package com.example.abroad.service;

import static com.example.abroad.TestConstants.STUDENT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.abroad.model.User;
import com.example.abroad.respository.AdminRepository;
import com.example.abroad.respository.StudentRepository;
import jakarta.servlet.http.HttpSession;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
public class UserServiceTest {

  private static final User USER = STUDENT;
  @Mock
  private HttpSession session;

  @Mock
  private AdminRepository adminRepository;
  @Mock
  private StudentRepository studentRepository;
  @Mock
  private PasswordEncoder passwordEncoder;

  private UserService service;

  @BeforeEach
  void setUp() {
    service = new UserService(adminRepository, studentRepository, passwordEncoder);
  }

  @Test
  void testGetUser() {
    when(session.getAttribute("user")).thenReturn(USER);
    var result = service.getUser(session);
    assertThat(result).isEqualTo(Optional.of(USER));
  }

  @Test
  void testGetUserNotPresent() {
    when(session.getAttribute("user")).thenReturn(null);
    var result = service.getUser(session);
    assertThat(result).isEqualTo(Optional.empty());
  }


}
