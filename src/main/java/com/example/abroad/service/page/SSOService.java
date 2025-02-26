package com.example.abroad.service.page;

import org.springframework.stereotype.Service;

import com.example.abroad.model.User;
import com.example.abroad.model.User.LocalUser;
import com.example.abroad.model.User.SSOUser;
import com.example.abroad.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Service
public class SSOService {
    private final UserService userService;

    public SSOService(UserService userService) {
        this.userService = userService;
    }

    public sealed interface SSOResult {
        record Success() implements SSOResult {}
        record AlreadyLoggedIn() implements SSOResult {}
        record SSOSessionNotPresent() implements SSOResult {}
        record UsernameTaken(String message) implements SSOResult {}
    }

    public SSOResult authenticateSSO(HttpServletRequest request, HttpSession session) {
        User user = userService.findUserFromSession(session).orElse(null);
        if (user != null) {
            return new SSOResult.AlreadyLoggedIn();
        }
        
        String uid = request.getHeader("uid");
        if (uid == null || uid.isBlank()) {
            return new SSOResult.SSOSessionNotPresent();
        }
        String displayName = request.getHeader("displayname");
        String email = request.getHeader("mail");
        
        User existingUser  = userService.findByUsername(uid).orElse(null);
        if (existingUser != null) {
            if (existingUser instanceof LocalUser) {
                return new SSOResult.UsernameTaken(
                    "A local account already exists with username '" + uid + "'. " +
                    "Please log in using local authentication or contact support."
                );
            } else if (existingUser instanceof SSOUser) {
                userService.saveUserToSession(existingUser, session);
                return new SSOResult.Success();
            }
        }
        
        SSOUser newUser = new SSOUser(uid, email, User.Role.STUDENT, displayName, User.Theme.DEFAULT);
        userService.save(newUser);
        userService.saveUserToSession(newUser, session);
        return new SSOResult.Success();
    }
}