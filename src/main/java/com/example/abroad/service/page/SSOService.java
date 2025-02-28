package com.example.abroad.service.page;

import java.nio.charset.StandardCharsets;
import java.net.URLEncoder;
import org.springframework.stereotype.Service;
import com.example.abroad.model.User;
import com.example.abroad.model.User.LocalUser;
import com.example.abroad.model.User.SSOUser;
import com.example.abroad.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Service
public record SSOService(UserService userService) {

  public sealed interface SSOResult {
    record Success(User user) implements SSOResult {}
    record AlreadyLoggedIn() implements SSOResult {}
    record SSOSessionNotPresent() implements SSOResult {}
    record UsernameTaken(String message) implements SSOResult {}
  }

  public SSOResult authenticateSSO(HttpServletRequest request, HttpSession session) {
    User user = userService.findUserFromSession(session).orElse(null);
    if (user != null) {
      return new SSOResult.AlreadyLoggedIn();
    }

    String username = request.getHeader("uid");
    if (username == null || username.isBlank()) {
      return new SSOResult.SSOSessionNotPresent();
    }
    String displayName = request.getHeader("displayname");
    String email = request.getHeader("mail");

    User existingUser = userService.findByUsername(username).orElse(null);
    if (existingUser != null) {
      if (existingUser instanceof LocalUser) {
        return new SSOResult.UsernameTaken(
          "A local account already exists with username '" + username + "'. " +
            "Please register using local authentication");
      } else if (existingUser instanceof SSOUser) {
        userService.saveUserToSession(existingUser, session);
        return new SSOResult.Success(existingUser);
      }
    }

    SSOUser newUser = new SSOUser(username, email, User.Role.STUDENT, displayName, User.Theme.DEFAULT);
    userService.save(newUser);
    userService.saveUserToSession(newUser, session);
    return new SSOResult.Success(newUser);
  }

  public static String buildLogoutUrl(String location, String infoMessage, String errorMessage) {
    if (errorMessage != null && !errorMessage.isBlank() && infoMessage != null && !infoMessage.isBlank()) {
      throw new IllegalArgumentException("Only one of errorMessage or infoMessage can be provided.");
    }
    String targetUrl = "https://beta.colab.duke.edu" + location;
    String shibLogoutUrl = "https://shib.oit.duke.edu/cgi-bin/logout.pl";
    if (errorMessage != null && !errorMessage.isBlank()) {
      targetUrl += "?error=" + encode(errorMessage);
    } else if (infoMessage != null && !infoMessage.isBlank()) {
      targetUrl += "?info=" + encode(infoMessage);
    }
    String logoutPlUrl = shibLogoutUrl + "?logoutWithoutPrompt=1&returnto=" + targetUrl;
    return encode(logoutPlUrl);
  }

  private static String encode(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }
}