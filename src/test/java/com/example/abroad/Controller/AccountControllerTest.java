package com.example.abroad.Controller;

import com.example.abroad.controller.AccountController;
import com.example.abroad.model.Admin;
import com.example.abroad.model.Student;
import com.example.abroad.respository.AdminRepository;
import com.example.abroad.respository.StudentRepository;
import com.example.abroad.service.FormatService;
import com.example.abroad.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.ui.Model;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountControllerTest {

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private FormatService formatService;

    @Mock
    private UserService userService;

    @Mock
    private HttpSession session;

    @Mock
    private Model model;

    private AccountController accountController;

    @BeforeEach
    void setUp() {
        accountController = new AccountController(adminRepository, studentRepository,
                passwordEncoder, formatService, userService);
    }

    @Test
    void getProfile_WhenUserNotAuthenticated_RedirectsToLogin() {
        // Arrange
        when(session.getAttribute("user")).thenReturn(null);

        // Act
        String result = accountController.getProfile(session, model, Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty());

        // Assert
        assertEquals("redirect:/login", result);
    }

    @Test
    void getProfile_WhenUserAuthenticated_ReturnsProfilePage() {
        // Arrange
        Student student = new Student("testuser", "password", "test@test.com", "Test User");
        when(session.getAttribute("user")).thenReturn(student);
        when(userService.getTheme(session)).thenReturn("light");

        // Act
        String result = accountController.getProfile(session, model, Optional.empty(),
                Optional.empty(), Optional.empty(), Optional.empty());

        // Assert
        assertEquals("profile :: page", result);
        verify(model).addAttribute("user", student);
        verify(model).addAttribute("formatter", formatService);
        verify(model).addAttribute("theme", "light");
    }

    @Test
    void updateProfile_WhenAdmin_UpdatesAdminProfile() {
        // Arrange
        Admin admin = new Admin("admin", "password", "old@test.com", "Old Name");
        when(session.getAttribute("user")).thenReturn(admin);

        // Act
        String result = accountController.updateProfile("New Name", "new@test.com", session, model);

        // Assert
        assertEquals("redirect:/profile?success=Profile updated successfully", result);
        verify(adminRepository).save(any(Admin.class));
        verify(session).setAttribute(eq("user"), any(Admin.class));
    }

    @Test
    void updateProfile_WhenStudent_UpdatesStudentProfile() {
        // Arrange
        Student student = new Student("student", "password", "old@test.com", "Old Name");
        when(session.getAttribute("user")).thenReturn(student);

        // Act
        String result = accountController.updateProfile("New Name", "new@test.com", session, model);

        // Assert
        assertEquals("redirect:/profile?success=Profile updated successfully", result);
        verify(studentRepository).save(any(Student.class));
        verify(session).setAttribute(eq("user"), any(Student.class));
    }

    @Test
    void changePassword_WhenCurrentPasswordIncorrect_ReturnsError() {
        // Arrange
        Student student = new Student("student", "oldpass", "test@test.com", "Test User");
        when(session.getAttribute("user")).thenReturn(student);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // Act
        String result = accountController.changePassword("wrongpass", "newpass", "newpass", session);

        // Assert
        assertEquals("redirect:/profile?error=Current password is incorrect", result);
    }

    @Test
    void changePassword_WhenPasswordsDontMatch_ReturnsError() {
        // Arrange
        Student student = new Student("student", "oldpass", "test@test.com", "Test User");
        when(session.getAttribute("user")).thenReturn(student);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        // Act
        String result = accountController.changePassword("currentpass", "newpass", "differentpass", session);

        // Assert
        assertEquals("redirect:/profile?error=New passwords do not match", result);
    }

    @Test
    void changePassword_WhenAdminPasswordUpdateSuccessful_UpdatesPassword() {
        // Arrange
        Admin admin = new Admin("admin", "oldpass", "admin@test.com", "Admin Name");
        when(session.getAttribute("user")).thenReturn(admin);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        // Act
        String result = accountController.changePassword("currentpass", "newpass", "newpass", session);

        // Assert
        assertEquals("redirect:/profile?success=Password updated successfully", result);
        verify(adminRepository).save(any(Admin.class));
        verify(session).setAttribute(eq("user"), any(Admin.class));
    }

    @Test
    void changePassword_WhenStudentPasswordUpdateSuccessful_UpdatesPassword() {
        // Arrange
        Student student = new Student("student", "oldpass", "student@test.com", "Student Name");
        when(session.getAttribute("user")).thenReturn(student);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        // Act
        String result = accountController.changePassword("currentpass", "newpass", "newpass", session);

        // Assert
        assertEquals("redirect:/profile?success=Password updated successfully", result);
        verify(studentRepository).save(any(Student.class));
        verify(session).setAttribute(eq("user"), any(Student.class));
    }
}