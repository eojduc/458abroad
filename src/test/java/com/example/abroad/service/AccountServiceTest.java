package com.example.abroad.service;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.abroad.model.Admin;
import com.example.abroad.model.Student;
import com.example.abroad.model.User;
import com.example.abroad.respository.AdminRepository;
import com.example.abroad.respository.StudentRepository;
import com.example.abroad.service.AccountService.ChangePassword;
import com.example.abroad.service.AccountService.GetProfile;
import com.example.abroad.service.AccountService.UpdateProfile;
import jakarta.servlet.http.HttpSession;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserService userService;

    @Mock
    private HttpSession session;

    private AccountService accountService;

    private final Admin testAdmin = new Admin(
            "admin1",
            "hashedpass123",
            "admin@test.com",
            "Test Admin"
    );

    private final Student testStudent = new Student(
            "student1",
            "hashedpass123",
            "student@test.com",
            "Test Student"
    );

    @BeforeEach
    void setUp() {
        accountService = new AccountService(
                adminRepository,
                studentRepository,
                passwordEncoder,
                userService
        );
    }

    @Test
    void getProfile_WhenUserNotFound_ReturnsUserNotFound() {
        when(userService.getUser(session)).thenReturn(Optional.empty());

        var result = accountService.getProfile(session);

        assertTrue(result instanceof GetProfile.UserNotFound);
    }

    @Test
    void getProfile_WhenUserFound_ReturnsSuccess() {
        when(userService.getUser(session)).thenReturn(Optional.of(testAdmin));

        var result = accountService.getProfile(session);

        assertTrue(result instanceof GetProfile.Success);
        assertEquals(testAdmin, ((GetProfile.Success) result).user());
    }

    @Test
    void updateProfile_WhenUserNotFound_ReturnsUserNotFound() {
        when(userService.getUser(session)).thenReturn(Optional.empty());

        var result = accountService.updateProfile("New Name", "new@email.com", session);

        assertTrue(result instanceof UpdateProfile.UserNotFound);
    }

    @Test
    void updateProfile_WhenUserIsAdmin_UpdatesSuccessfully() {
        when(userService.getUser(session)).thenReturn(Optional.of(testAdmin));

        var expectedAdmin = new Admin(
                testAdmin.username(),
                testAdmin.password(),
                "new@email.com",
                "New Name"
        );
        when(adminRepository.save(any(Admin.class))).thenReturn(expectedAdmin);

        var result = accountService.updateProfile("New Name", "new@email.com", session);

        assertTrue(result instanceof UpdateProfile.Success);
        var success = (UpdateProfile.Success) result;
        assertEquals("New Name", success.updatedUser().displayName());
        assertEquals("new@email.com", success.updatedUser().email());
        verify(adminRepository).save(any(Admin.class));
    }

    @Test
    void updateProfile_WhenUserIsStudent_UpdatesSuccessfully() {
        when(userService.getUser(session)).thenReturn(Optional.of(testStudent));

        var expectedStudent = new Student(
                testStudent.username(),
                testStudent.password(),
                "new@email.com",
                "New Name"
        );
        when(studentRepository.save(any(Student.class))).thenReturn(expectedStudent);

        var result = accountService.updateProfile("New Name", "new@email.com", session);

        assertTrue(result instanceof UpdateProfile.Success);
        var success = (UpdateProfile.Success) result;
        assertEquals("New Name", success.updatedUser().displayName());
        assertEquals("new@email.com", success.updatedUser().email());
        verify(studentRepository).save(any(Student.class));
    }

    @Test
    void changePassword_WhenUserNotFound_ReturnsUserNotFound() {
        when(userService.getUser(session)).thenReturn(Optional.empty());

        var result = accountService.changePassword("oldPass", "newPass", "newPass", session);

        assertTrue(result instanceof ChangePassword.UserNotFound);
    }

    @Test
    void changePassword_WhenCurrentPasswordIncorrect_ReturnsIncorrectPassword() {
        when(userService.getUser(session)).thenReturn(Optional.of(testAdmin));
        when(passwordEncoder.matches("wrongPass", testAdmin.password())).thenReturn(false);

        var result = accountService.changePassword("wrongPass", "newPass", "newPass", session);

        assertTrue(result instanceof ChangePassword.IncorrectPassword);
    }

    @Test
    void changePassword_WhenPasswordsDontMatch_ReturnsPasswordMismatch() {
        when(userService.getUser(session)).thenReturn(Optional.of(testAdmin));
        when(passwordEncoder.matches("currentPass", testAdmin.password())).thenReturn(true);

        var result = accountService.changePassword("currentPass", "newPass", "differentPass", session);

        assertTrue(result instanceof ChangePassword.PasswordMismatch);
    }

    @Test
    void changePassword_WhenUserIsAdmin_UpdatesSuccessfully() {
        when(userService.getUser(session)).thenReturn(Optional.of(testAdmin));
        when(passwordEncoder.matches("currentPass", testAdmin.password())).thenReturn(true);
        when(passwordEncoder.encode("newPass")).thenReturn("newHashedPass");

        var expectedAdmin = new Admin(
                testAdmin.username(),
                "newHashedPass",
                testAdmin.email(),
                testAdmin.displayName()
        );
        when(adminRepository.save(any(Admin.class))).thenReturn(expectedAdmin);

        var result = accountService.changePassword("currentPass", "newPass", "newPass", session);

        assertTrue(result instanceof ChangePassword.Success);
        var success = (ChangePassword.Success) result;
        assertEquals("newHashedPass", success.updatedUser().password());
        verify(adminRepository).save(any(Admin.class));
    }

    @Test
    void changePassword_WhenUserIsStudent_UpdatesSuccessfully() {
        when(userService.getUser(session)).thenReturn(Optional.of(testStudent));
        when(passwordEncoder.matches("currentPass", testStudent.password())).thenReturn(true);
        when(passwordEncoder.encode("newPass")).thenReturn("newHashedPass");

        var expectedStudent = new Student(
                testStudent.username(),
                "newHashedPass",
                testStudent.email(),
                testStudent.displayName()
        );
        when(studentRepository.save(any(Student.class))).thenReturn(expectedStudent);

        var result = accountService.changePassword("currentPass", "newPass", "newPass", session);

        assertTrue(result instanceof ChangePassword.Success);
        var success = (ChangePassword.Success) result;
        assertEquals("newHashedPass", success.updatedUser().password());
        verify(studentRepository).save(any(Student.class));
    }
}