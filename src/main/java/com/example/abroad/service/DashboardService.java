package com.example.abroad.service;
import com.example.abroad.model.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

@Service
public record DashboardService(UserService userService) {

    public sealed interface GetDashboard permits
            GetDashboard.StudentDashboard,
            GetDashboard.AdminDashboard,
            GetDashboard.NotLoggedIn {

        record StudentDashboard(User user) implements GetDashboard {}
        record AdminDashboard(User user) implements GetDashboard {}
        record NotLoggedIn() implements GetDashboard {}
    }

    public GetDashboard getDashboard(HttpSession session) {
        User user = userService.getUser(session).orElse(null);
        if (user == null) {
            return new GetDashboard.NotLoggedIn();
        }

        if (user.isAdmin()) {
            return new GetDashboard.AdminDashboard(user);
        }

        return new GetDashboard.StudentDashboard(user);
    }

    public void setTheme(String theme, HttpSession session) {
        userService.setTheme(theme, session);
    }

    public String getTheme(HttpSession session) {
        return userService.getTheme(session);
    }
}