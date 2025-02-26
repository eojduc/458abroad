package com.example.abroad.service.page;
import com.example.abroad.model.User;
import com.example.abroad.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

@Service
public record DashboardService(UserService userService) {

    public sealed interface GetDashboard {
        record StudentDashboard(User user) implements GetDashboard {}
        record AdminDashboard(User user) implements GetDashboard {}
        record NotLoggedIn() implements GetDashboard {}
    }

    public GetDashboard getDashboard(HttpSession session) {
        return userService.findUserFromSession(session)
          .map(user -> user.isAdmin() ?
            new GetDashboard.AdminDashboard(user) :
            new GetDashboard.StudentDashboard(user))
          .orElse(new GetDashboard.NotLoggedIn());
    }

}