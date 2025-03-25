package com.example.abroad.service.page;

import com.example.abroad.model.User;
import com.example.abroad.model.User.Theme;
import com.example.abroad.service.AuditService;
import com.example.abroad.service.UserService;

import jakarta.servlet.http.HttpSession;

import org.slf4j.MDC;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public record AuthService(
  UserService userService,
  SSOService ssoService,
  PasswordEncoder passwordEncoder,
  AuditService auditService
) {

  public sealed interface Login {
    record Success(User user) implements Login {}
    record InvalidCredentials() implements Login {}
  }

  public Login login(String username, String password, HttpSession session) {
    var user = userService.findByUsername(username).orElse(null);
    if (
      user == null ||
      !(user instanceof User.LocalUser localUser) ||
      !passwordEncoder.matches(password, localUser.password())
    ) {
      return new Login.InvalidCredentials();
    }
    userService.saveUserToSession(localUser, session);
    
    MDC.put("username", user.username());
    auditService.logEvent("Local User " + username + " logged in.");

    return new Login.Success(localUser);
  }

  public sealed interface CheckLoginStatus permits
    CheckLoginStatus.AlreadyLoggedIn,
    CheckLoginStatus.NotLoggedIn {
    record AlreadyLoggedIn() implements CheckLoginStatus {}
    record NotLoggedIn() implements CheckLoginStatus {}
  }

  public CheckLoginStatus checkLoginStatus(HttpSession session) {
    User user = userService.findUserFromSession(session).orElse(null);
    if (user != null) {
      return new CheckLoginStatus.AlreadyLoggedIn();
    }
    return new CheckLoginStatus.NotLoggedIn();
  }

  public sealed interface Logout {
    record LocalUserSuccess() implements Logout {}
    record SSOUserSuccess(String redirectUrl) implements Logout {}
    record NotLoggedIn() implements Logout {}
  }

  public Logout logout(HttpSession session) {
    User user = userService.findUserFromSession(session).orElse(null);
    if (user == null) {
      return new Logout.NotLoggedIn();
    }
    
    session.invalidate();
    if (user instanceof User.SSOUser) {
      String redirectUrl = ssoService.buildLogoutUrl("/login", "You have been logged out.", "");
      return new Logout.SSOUserSuccess(redirectUrl);
    }
    return new Logout.LocalUserSuccess();
  }

  public sealed interface RegisterResult {

    record Success() implements RegisterResult {}
    record UsernameExists() implements RegisterResult {}
    record EmailExists() implements RegisterResult {}
    record PasswordMismatch() implements RegisterResult {}
    record AuthenticationError(Exception error) implements RegisterResult {}
  }

  public RegisterResult registerUser(
    String username,
    String displayName,
    String email,
    String password,
    String confirmPassword,
    HttpSession session) {

    if (!password.equals(confirmPassword)) {
      return new RegisterResult.PasswordMismatch();
    }
    var users = userService.findAll();
    if (users.stream().anyMatch(u -> u.username().equals(username))) {
      return new RegisterResult.UsernameExists();
    }
    if (users.stream().anyMatch(u -> u.email().equals(email))) {
      return new RegisterResult.EmailExists();
    }
    var hashedPassword = passwordEncoder.encode(password);
    var user = new User.LocalUser(username, hashedPassword, email, displayName, Theme.DEFAULT);
    userService.save(user);
    userService.saveUserToSession(user, session);
    
    MDC.put("username", username);
    auditService.logEvent("Local User " + username + " registered.");

    return new RegisterResult.Success();
  }

}
