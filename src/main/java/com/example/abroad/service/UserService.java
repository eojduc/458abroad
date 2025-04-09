package com.example.abroad.service;

import com.example.abroad.model.User;
import com.example.abroad.model.User.Course;
import com.example.abroad.model.User.LocalUser;
import com.example.abroad.model.User.Theme;
import com.example.abroad.respository.CourseRepository;
import com.example.abroad.respository.LocalUserRepository;
import com.example.abroad.respository.RoleRepository;
import com.example.abroad.respository.SSOUserRepository;
import jakarta.persistence.Id;
import jakarta.servlet.http.HttpSession;
import com.example.abroad.model.User.Role;
import com.example.abroad.model.User.Role.ID;
import com.example.abroad.model.User.Role.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public record UserService(
  LocalUserRepository localUserRepository,
  SSOUserRepository ssoUserRepository,
  RoleRepository roleRepository,
  CourseRepository courseRepository
) {

  public Optional<User> findUserFromSession(HttpSession session){
    return Optional.ofNullable((User) session.getAttribute("user"));
  }

  public void saveUserToSession(User user, HttpSession session) {
    session.setAttribute("user", user);
  }

  public Optional<? extends User> findByUsername(String username) {
    var user = localUserRepository.findByUsername(username);
    if (user.isPresent()) {
      return user;
    }
    return ssoUserRepository.findByUsername(username);
  }

  public List<? extends User> findAll() {
    return Stream.concat(localUserRepository.findAll().stream(), ssoUserRepository.findAll().stream())
      .toList();
  }

  public User save(User user) {
    return switch (user) {
      case User.LocalUser localUser -> localUserRepository.save(localUser);
      case User.SSOUser ssoUser -> ssoUserRepository.save(ssoUser);
    };
  }

  public User previewUser() {
    return new LocalUser("previewUser", "NA", "preview@gmail.com", "Preview",
                          Theme.DEFAULT, "previewUser");
  }

  public Boolean isAdmin(User user) {
    return roleRepository.findById_Username(user.username()).stream()
      .anyMatch(role -> role.type().equals(User.Role.Type.ADMIN));
  }

  public Boolean isStudent(User user) {
    return roleRepository.findById_Username(user.username()).isEmpty();
  }

  public Boolean isHeadAdmin(User user) {
    return user.username().equals("admin");
  }

  public void deleteUser(User user) {
    switch (user) {
      case User.LocalUser localUser -> localUserRepository.delete(localUser);
      case User.SSOUser ssoUser -> ssoUserRepository.delete(ssoUser);
    }
  }

  public Boolean isReviewer(User user) {
    return roleRepository.findById_Username(user.username()).stream()
      .anyMatch(role -> role.type().equals(User.Role.Type.REVIEWER));
  }


  public Boolean isFaculty(User user) {
    return roleRepository.findById_Username(user.username()).stream()
      .anyMatch(role -> role.type().equals(User.Role.Type.FACULTY));
  }


  public Boolean isPartner(User user) {
    return roleRepository.findById_Username(user.username()).stream()
            .anyMatch(role -> role.type().equals(User.Role.Type.PARTNER));
  }

  public void setTheme(User.Theme theme, HttpSession session) {
    var user = findUserFromSession(session).orElse(null);
    if (user == null) {
      return;
    }
    var newUser = user.withTheme(theme);
    save(newUser);
    saveUserToSession(newUser, session);
  }

  public void addRole(User user, Role.Type roleType) {
    // Check if user already has this role
    if (roleRepository.findById_UsernameAndId_Type(user.username(), roleType).isEmpty()) {
      // Create and save new role using the existing constructor
      Role newRole = new Role(roleType, user.username());
      roleRepository.save(newRole);
    }
  }

  public void removeRole(User user, User.Role.Type roleType) {
    roleRepository.findById_UsernameAndId_Type(user.username(), roleType)
            .ifPresent(roleRepository::delete);
  }

  public List<? extends User> findUsersWithRole(User.Role.Type roleType) {
    return roleRepository.findById_Type(roleType)
        .stream()
        .map(role -> findByUsername(role.username()))
        .filter(Optional::isPresent)  // Filter out empty Optionals
        .map(Optional::get)           // Unwrap the Optionals
        .toList();
  }

  public void saveCourse(String courseCode, String username, String grade) {
    courseRepository.save(new Course(courseCode, username, grade));
  }

  public List<Course> findCoursesByUsername(String username) {
    return courseRepository.findById_Username(username);
  }
}