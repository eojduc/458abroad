package com.example.abroad.service;

import com.example.abroad.exception.EmailAlreadyInUseException;
import com.example.abroad.exception.UsernameAlreadyInUseException;
import com.example.abroad.model.Admin;
import com.example.abroad.model.Student;
import com.example.abroad.model.User;
import com.example.abroad.respository.AdminRepository;
import com.example.abroad.respository.StudentRepository;
import jakarta.servlet.http.HttpSession;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public record UserService(
  AdminRepository adminRepository,
  StudentRepository studentRepository,
  PasswordEncoder passwordEncoder
) {

  private static final String USER_SESSION_ATTRIBUTE = "user";



  public Optional<? extends User> getUser(HttpSession session){
//    return Optional.ofNullable((User) session.getAttribute(USER_SESSION_ATTRIBUTE));
    return adminRepository.findAll().stream().findFirst();
  }

  public Student registerStudent(String username, String displayName, String email, String password) {
    checkUsernameAndEmailAvailability(username, email);

    String hashedPassword = passwordEncoder.encode(password);
    System.out.println("Encoded password during registration: " + hashedPassword);
    Student student = new Student(username, hashedPassword, email, displayName);
    return studentRepository.save(student);
  }

  public Admin createAdmin(String username, String email, String password, HttpSession session) {

    String creatorUsername = (String) session.getAttribute("username");
    System.out.println("creator's username: " + creatorUsername);
    Optional<Admin> optionalCreator = adminRepository.findByUsername(creatorUsername);
    User creator = null;
    if (optionalCreator.isPresent()) {
      creator = optionalCreator.get();
    }

    if (creator == null) {
      throw new IllegalStateException("Admin not found in session");
    }
    System.out.println("Creator username: " + creatorUsername);
    System.out.println("Creator role: " + creator.role());
    System.out.println("Is creator an admin? " + creator.isAdmin());

    if (!creator.isAdmin()) {
      throw new IllegalStateException("Only admins can create other admins");
    }

    checkUsernameAndEmailAvailability(username, email);

    String hashedPassword = passwordEncoder.encode(password);
    Admin admin = new Admin(username, hashedPassword, email, username);
    return adminRepository.save(admin);
  }

  private void checkUsernameAndEmailAvailability(String username, String email) {
    if (adminRepository.existsByUsername(username) ||
      studentRepository.existsByUsername(username)) {
      throw new UsernameAlreadyInUseException("Username already taken: " + username);
    }

    if (adminRepository.existsByEmail(email) ||
      studentRepository.existsByEmail(email)) {
      throw new EmailAlreadyInUseException("Email already registered: " + email);
    }
  }

//  public User authenticateUser(String username, String password) {
//    User user = findByUsername(username)
//      .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
//
//    System.out.println("Stored password hash: " + user.password());
//    System.out.println(
//      "Password match result: " + passwordEncoder.matches(password, user.password()));
//
//    if (!passwordEncoder.matches(password, user.password())) {
//      throw new IncorrectPasswordException("Incorrect password");
//    }
//
//    return user;
//  }

  public Optional<Student> findByUsername(String username) {

    Optional<Student> student = studentRepository.findByUsername(username);
    return student;

  }

}