package com.example.abroad.service.page;
import com.example.abroad.model.User;
import com.example.abroad.service.ULinkTranscriptService;
import com.example.abroad.service.UserService;
import com.example.abroad.service.page.DashboardService.GetDashboard.SSOUsernameTaken;
import com.example.abroad.service.page.SSOService.SSOResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

@Service
public record DashboardService(UserService userService, SSOService ssoService, ULinkTranscriptService ulinkService) {

  public sealed interface GetDashboard {
    record StudentDashboard(User user) implements GetDashboard {}
    record AdminDashboard(User user, Boolean isAdmin, Boolean isHeadAdmin) implements GetDashboard {}
    record SSOUsernameTaken(String redirect) implements GetDashboard {}
    record NotLoggedIn() implements GetDashboard {}
    record PartnerDashboard(User user) implements GetDashboard {}
  }

  public GetDashboard getDashboard(HttpSession session, HttpServletRequest request) {
    SSOResult ssoResult = ssoService.authenticateSSO(request, session);
    if (ssoResult instanceof SSOResult.UsernameTaken(String message)) {
      String redirectUrl = ssoService.buildLogoutUrl("/register", "", message);
      return new SSOUsernameTaken("/Shibboleth.sso/Logout?return=" + redirectUrl);
    }
    var user = userService.findUserFromSession(session).orElse(null);
    if (user == null) {
      return new GetDashboard.NotLoggedIn();
    }

    if (userService.isPartner(user)) {
      System.out.println("user is partner");
      return new GetDashboard.PartnerDashboard(user);
    }

    if (!userService.isStudent(user)) {
      return new GetDashboard.AdminDashboard(user, userService.isAdmin(user), userService.isHeadAdmin(user));
    }
    return new GetDashboard.StudentDashboard(user);
  }

}