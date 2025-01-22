package com.example.abroad.service;

import com.example.abroad.exception.EmailAlreadyInUseException;
import com.example.abroad.exception.IncorrectPasswordException;
import com.example.abroad.exception.UsernameAlreadyInUseException;
import com.example.abroad.model.Admin;
import com.example.abroad.model.Student;
import com.example.abroad.model.User;
import com.example.abroad.respository.AdminRepository;
import com.example.abroad.respository.StudentRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {
  private static final String USER_SESSION_ATTRIBUTE = "user";

  private final AdminRepository adminRepository;
  private final StudentRepository studentRepository;
  private final PasswordEncoder passwordEncoder;  // Using Spring's PasswordEncoder instead of direct BCrypt

  public UserService(AdminRepository adminRepository,
                     StudentRepository studentRepository,
                     PasswordEncoder passwordEncoder) {
    this.adminRepository = adminRepository;
    this.studentRepository = studentRepository;
    this.passwordEncoder = passwordEncoder;
  }

  public void setUser(HttpServletRequest request, User user) {
    request.getSession().setAttribute(USER_SESSION_ATTRIBUTE, user);
  }

  public Optional<User> getUser(HttpServletRequest request) {
    var attribute = request.getSession().getAttribute(USER_SESSION_ATTRIBUTE);
    if (attribute instanceof User user) {
      return Optional.of(user);
    }
    return Optional.empty();
  }

  @Transactional
  public Student registerStudent(String username, String email, String password) {
    checkUsernameAndEmailAvailability(username, email);

    String hashedPassword = passwordEncoder.encode(password);
    Student student = new Student(username, email, hashedPassword, username);
    return studentRepository.save(student);
  }

  @Transactional
  public Admin createAdmin(String username, String email, String password, User creator) {
    if (creator == null || !creator.isAdmin()) {
      throw new IllegalStateException("Only admins can create other admins");
    }

    checkUsernameAndEmailAvailability(username, email);

    String hashedPassword = passwordEncoder.encode(password);
    Admin admin = new Admin(username, email, hashedPassword, username);
    return adminRepository.save(admin);
  }

  private void checkUsernameAndEmailAvailability(String username, String email) {
    // Check both repositories for existing username
    if (adminRepository.existsByUsername(username) ||
            studentRepository.existsByUsername(username)) {
      throw new UsernameAlreadyInUseException("Username already taken: " + username);
    }

    // Check both repositories for existing email
    if (adminRepository.existsByEmail(email) ||
            studentRepository.existsByEmail(email)) {
      throw new EmailAlreadyInUseException("Email already registered: " + email);
    }
  }

  @Transactional(readOnly = true)
  public User authenticateUser(String username, String password) {
    User user = findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

    if (!passwordEncoder.matches(password, user.password())) {
      throw new IncorrectPasswordException("Incorrect password");
    }

    return user;
  }

  @Transactional(readOnly = true)
  public Optional<User> findByUsername(String username) {
    Optional<Admin> admin = adminRepository.findByUsername(username);
    if (admin.isPresent()) {
      return Optional.of(admin.get());
    }

    Optional<Student> student = studentRepository.findByUsername(username);
    return student.map(s -> s);
  }

  @Transactional(readOnly = true)
  public Optional<User> findByEmail(String email) {
    Optional<Admin> admin = adminRepository.findByEmail(email);
    if (admin.isPresent()) {
      return Optional.of(admin.get());
    }

    Optional<Student> student = studentRepository.findByEmail(email);
    return student.map(s -> s);
  }

  @Transactional(readOnly = true)
  public boolean isEmailTaken(String email) {
    return adminRepository.existsByEmail(email) ||
            studentRepository.existsByEmail(email);
  }

  @Transactional(readOnly = true)
  public boolean isUsernameTaken(String username) {
    return adminRepository.existsByUsername(username) ||
            studentRepository.existsByUsername(username);
  }
}