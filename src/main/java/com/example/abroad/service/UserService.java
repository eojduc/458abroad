package com.example.abroad.service;

import com.example.abroad.model.User;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * This class is used to manage the user session
 */
@Service
public class UserService {

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

}
