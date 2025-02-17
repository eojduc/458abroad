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
        User user = userService.findUserFromSession(session).orElse(null);
        if (user == null) {
            return new GetDashboard.NotLoggedIn();
        }

        if (user.role() == User.Role.ADMIN) {
            return new GetDashboard.AdminDashboard(user);
        }

        return new GetDashboard.StudentDashboard(user);
    }

    public String getTheme(HttpSession session) {
        return userService.getTheme(session);
    }
}