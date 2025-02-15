package com.example.abroad.service;

import com.example.abroad.model.User;
import com.example.abroad.respository.LocalUserRepository;
import com.example.abroad.respository.SSOUserRepository;
import jakarta.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;

@Service
public record UserService(
  LocalUserRepository localUserRepository,
  SSOUserRepository ssoUserRepository
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

  public void save(User user) {
    switch (user) {
      case User.LocalUser localUser -> localUserRepository.save(localUser);
      case User.SSOUser ssoUser -> ssoUserRepository.save(ssoUser);
    }
  }
  enum DaisyTheme {
    LIGHT, DARK, CUPCAKE, BUMBLEBEE, EMERALD, CORPORATE, SYNTHWAVE, RETRO, CYBERPUNK, VALENTINE,
    HALLOWEEN, GARDEN, FOREST, AQUA, LOFI, PASTEL, FANTASY, WIREFRAME, BLACK, LUXURY, DRACULA,
    CMYK, AUTUMN, BUSINESS, ACID, LEMONADE, NIGHT, COFFEE, WINTER, DIM, NORD, SUNSET, DEFAULT
  }

  public void setTheme(String theme , HttpSession session) {
    if (theme.equals(DaisyTheme.DEFAULT)) {
      session.setAttribute("theme", "");
    }
    else if (Arrays.stream(DaisyTheme.values()).map(Enum::name).anyMatch(theme::equals)) { // Check if theme is a valid DaisyTheme
      session.setAttribute("theme", theme);
    }
    else {
      session.setAttribute("theme", "");
    }
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