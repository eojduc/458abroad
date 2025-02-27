package com.example.abroad.service.page;
import com.example.abroad.model.User;
import com.example.abroad.service.UserService;
import com.example.abroad.service.page.DashboardService.GetDashboard.SSOUsernameTaken;
import com.example.abroad.service.page.SSOService.SSOResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

@Service
public record DashboardService(UserService userService, SSOService ssoService) {

  public sealed interface GetDashboard {
    record StudentDashboard(User user) implements GetDashboard {}
    record AdminDashboard(User user) implements GetDashboard {}
    record SSOUsernameTaken(String redirect) implements GetDashboard {}
    record NotLoggedIn() implements GetDashboard {}
  }

  public GetDashboard getDashboard(HttpSession session, HttpServletRequest request) {
    var user = userService.findUserFromSession(session).orElse(null);
    if (user == null) {
      SSOResult ssoResult = ssoService.authenticateSSO(request, session);
      if (ssoResult instanceof SSOResult.UsernameTaken(String message)) {
        String redirectUrl = SSOService.buildLogoutUrl("/register", "", message);
        return new SSOUsernameTaken("/Shibboleth.sso/Logout?return=" + redirectUrl);
      }
    }
    var newUser = userService.findUserFromSession(session).orElse(null);
    if (newUser == null) {
      return new GetDashboard.NotLoggedIn();
    }
    if (newUser.isAdmin()) {
      return new GetDashboard.AdminDashboard(user);
    }
    return new GetDashboard.StudentDashboard(user);
  }

}