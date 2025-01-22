package com.example.abroad.service;

import com.example.abroad.exception.EmailAlreadyInUseException;
import com.example.abroad.exception.UsernameAlreadyInUseException;
import com.example.abroad.model.User;
import com.example.abroad.respository.AdminRepository;
import com.example.abroad.respository.StudentRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;

import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

/**
 * This class is used to manage the user session
 */
@Service
public record UserService() {
  private final AdminRepository adminRepository;
  private final StudentRepository studentRepository;

  public void setUser(HttpServletRequest request, User user) {
    request.getSession().setAttribute("user", user);
  }

  public Optional<User> getUser(HttpServletRequest request) {
    var attribute = request.getSession().getAttribute("user");
    if (attribute instanceof User user) {
      return Optional.of(user);
    }
    return Optional.empty();
  }

  public User registerUser(String username, String email, String password) {
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

    // Hash the password
    String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

    // By default, register as student
    Student student = new Student(username, email, hashedPassword, username); // assuming displayName is same as username initially
    return studentRepository.save(student);
  }
  public User authenticateUser(String username, String password) {
    // Try admin first
    Optional<Admin> admin = adminRepository.findByUsername(username);
    if (admin.isPresent()) {
      if (!BCrypt.checkpw(password, admin.get().password())) {
        throw new IncorrectPasswordException("Incorrect password");
      }
      return admin.get();
    }

    // Try student if admin not found
    Optional<Student> student = studentRepository.findByUsername(username);
    if (student.isPresent()) {
      if (!BCrypt.checkpw(password, student.get().password())) {
        throw new IncorrectPasswordException("Incorrect password");
      }
      return student.get();
    }

    throw new UsernameNotFoundException("User not found: " + username);
  }
}
