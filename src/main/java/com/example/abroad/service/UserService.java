package com.example.abroad.service;

import com.example.abroad.model.User;
import com.example.abroad.model.User.Theme;
import com.example.abroad.respository.LocalUserRepository;
import com.example.abroad.respository.SSOUserRepository;
import jakarta.servlet.http.HttpSession;
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
  SSOUserRepository ssoUserRepository
) {

  private static final Logger log = LoggerFactory.getLogger(UserService.class);

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

  public void setTheme(User.Theme theme, HttpSession session) {
    var user = findUserFromSession(session).orElse(null);
    if (user == null) {
      return;
    }
    var newUser = switch (user) {
      case User.LocalUser localUser -> save(new User.LocalUser(
        localUser.username(),
        localUser.password(),
        localUser.email(),
        localUser.role(),
        localUser.displayName(),
        theme
      ));
      case User.SSOUser ssoUser -> save(new User.SSOUser(
        ssoUser.username(),
        ssoUser.email(),
        ssoUser.role(),
        ssoUser.displayName(),
        theme
      ));
    };
    saveUserToSession(newUser, session);
  }

  public String getTheme(HttpSession session) {
    if (session.getAttribute("theme") == null) {
      return "";
    }
    else if (session.getAttribute("theme") instanceof String string) {
      return string.toLowerCase();
    }
    else {
      return "";
    }
  }


}