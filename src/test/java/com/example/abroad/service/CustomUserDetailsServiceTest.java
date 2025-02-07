package com.example.abroad.service;
import com.example.abroad.model.Admin;
import com.example.abroad.model.Role;
import com.example.abroad.model.Student;
import com.example.abroad.respository.AdminRepository;
import com.example.abroad.respository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private AdminRepository adminRepository;

    private CustomUserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        userDetailsService = new CustomUserDetailsService(studentRepository, adminRepository);
    }

    @Test
    void loadUserByUsername_WhenAdminExists_ReturnsAdminUserDetails() {
        // Arrange
        String username = "adminUser";
        String password = "adminPass";
        Admin admin = new Admin(username, password, "admin@test.com", "Admin User");
        when(adminRepository.findByUsername(username)).thenReturn(Optional.of(admin));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // Assert
        assertNotNull(userDetails);
        assertEquals(username, userDetails.getUsername());
        assertEquals(password, userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Role.ROLE_ADMIN.name())));
        verify(studentRepository, never()).findByUsername(username);
    }

    @Test
    void loadUserByUsername_WhenStudentExists_ReturnsStudentUserDetails() {
        // Arrange
        String username = "studentUser";
        String password = "studentPass";
        Student student = new Student(username, password, "student@test.com", "Student User");
        when(adminRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(studentRepository.findByUsername(username)).thenReturn(Optional.of(student));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // Assert
        assertNotNull(userDetails);
        assertEquals(username, userDetails.getUsername());
        assertEquals(password, userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Role.ROLE_STUDENT.name())));
    }

    @Test
    void loadUserByUsername_WhenUserDoesNotExist_ThrowsException() {
        // Arrange
        String username = "nonexistentUser";
        when(adminRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(studentRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(UsernameNotFoundException.class, () -> {
            userDetailsService.loadUserByUsername(username);
        });

        assertEquals("User not found: " + username, exception.getMessage());
        verify(adminRepository).findByUsername(username);
        verify(studentRepository).findByUsername(username);
    }

    @Test
    void loadUserByUsername_ChecksAdminRepositoryFirst() {
        // Arrange
        String username = "testUser";
        Admin admin = new Admin(username, "password", "admin@test.com", "Admin User");
        when(adminRepository.findByUsername(username)).thenReturn(Optional.of(admin));

        // Act
        userDetailsService.loadUserByUsername(username);

        // Assert
        verify(adminRepository).findByUsername(username);
        verify(studentRepository, never()).findByUsername(username);
    }

    @Test
    void loadUserByUsername_CreatesCorrectAuthorities() {
        // Arrange
        String username = "testUser";
        Student student = new Student(username, "password", "student@test.com", "Student User");
        when(adminRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(studentRepository.findByUsername(username)).thenReturn(Optional.of(student));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // Assert
        assertEquals(1, userDetails.getAuthorities().size());
        assertEquals(Role.ROLE_STUDENT.name(),
                userDetails.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void loadUserByUsername_HandlesNullPassword() {
        // Arrange
        String username = "testUser";
        String defaultPassword = "";  // or some other default value that your system uses
        Student student = new Student(username, defaultPassword, "student@test.com", "Student User");
        when(adminRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(studentRepository.findByUsername(username)).thenReturn(Optional.of(student));

        // Act
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        // Assert
        assertNotNull(userDetails);
        assertEquals(defaultPassword, userDetails.getPassword());
    }
}