package com.example.abroad.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.abroad.TestConstants;
import com.example.abroad.model.User;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class UserServiceTest {
  private static final User USER = TestConstants.STUDENT;

  //mock should be deep stubs
  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private HttpServletRequest request;


  private UserService service;

  @BeforeEach
  void setUp() {
    service = new UserService();
  }

  @Test
  void testSetUser() {
    service.setUser(request, USER);
    verify(request.getSession()).setAttribute("user", USER);
  }

  @Test
  void testGetUser() {
    when(request.getSession().getAttribute("user")).thenReturn(USER);
    var result = service.getUser(request);
    assertThat(result).isEqualTo(Optional.of(USER));
  }

  @Test
  void testGetUserNotPresent() {
    when(request.getSession().getAttribute("user")).thenReturn(null);
    var result = service.getUser(request);
    assertThat(result).isEqualTo(Optional.empty());
  }



}
