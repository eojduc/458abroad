package com.example.abroad.service;

import com.example.abroad.model.User;
import com.example.abroad.respository.StudentRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import org.springframework.stereotype.Service;
/**
 * This class is used to get the user from the request. Other auth operations can go here as well
 */
@Service
public class UserService {

  private final StudentRepository studentRepository;

  public UserService(StudentRepository studentRepository) {
    this.studentRepository = studentRepository;
  }

  public void setUser(HttpServletRequest request, User user) {
    request.getSession().setAttribute("user", user);
  }

  public Optional<? extends User> getUser(HttpServletRequest request) {
    var attribute = request.getSession().getAttribute("user");
    if (attribute instanceof User) {
      return Optional.of((User) attribute);
    }
    return Optional.empty();
  }

}
