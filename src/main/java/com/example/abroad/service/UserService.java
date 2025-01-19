package com.example.abroad.service;

import com.example.abroad.model.User;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
/**
 * This class is used to manage the user session
 */
public class UserService {

  public static void setUser(HttpServletRequest request, User user) {
    request.getSession().setAttribute("user", user);
  }

  public static Optional<User> getUser(HttpServletRequest request) {
    var attribute = request.getSession().getAttribute("user");
    if (attribute instanceof User) {
      return Optional.of((User) attribute);
    }
    return Optional.empty();
  }

}
