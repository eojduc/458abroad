package com.example.abroad.service.page;
import com.example.abroad.model.User;
import com.example.abroad.service.AuditService;
import com.example.abroad.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public record AccountService(
        PasswordEncoder passwordEncoder,
        UserService userService,
        AuditService auditService
) {
    public sealed interface GetProfile permits GetProfile.Success, GetProfile.UserNotFound {
        record Success(User user) implements GetProfile {}
        record UserNotFound() implements GetProfile {}
    }

    public GetProfile getProfile(HttpSession session) {
        User user = userService.findUserFromSession(session).orElse(null);
        if (user == null) {
            return new GetProfile.UserNotFound();
        }
        return new GetProfile.Success(user);
    }

    public sealed interface UpdateProfile permits
            UpdateProfile.Success,
            UpdateProfile.UserNotFound {

        record Success(User updatedUser) implements UpdateProfile {}
        record UserNotFound() implements UpdateProfile {}
    }

    public UpdateProfile updateProfile(String displayName, String email, HttpSession session) {
        User user = userService.findUserFromSession(session).orElse(null);
        if (user == null) {
            return new UpdateProfile.UserNotFound();
        }

        return switch (user) {
            case User.LocalUser localUser -> {
                var updatedUser = new User.LocalUser(
                        localUser.username(),
                        localUser.password(),
                        email,
                        displayName,
                        localUser.theme()
                );
                userService.save(updatedUser);
                auditService.logEvent("User " + user.username() + " successfully updated account information");
                yield new UpdateProfile.Success(updatedUser);
            }
            case User.SSOUser ssoUser -> {
                var updatedUser = new User.SSOUser(
                        ssoUser.username(),
                        email,
                        displayName,
                        ssoUser.theme()
                );
                userService.save(updatedUser);
                auditService.logEvent("User " + user.username() + " successfully updated account information");
                yield new UpdateProfile.Success(updatedUser);
            }
        };
    }

    public sealed interface ChangePassword  {

        record Success(User updatedUser) implements ChangePassword {}
        record UserNotFound() implements ChangePassword {}
        record IncorrectPassword() implements ChangePassword {}
        record PasswordMismatch() implements ChangePassword {}
        record NotLocalUser() implements ChangePassword {}
    }

    public ChangePassword changePassword(
            String currentPassword,
            String newPassword,
            String confirmPassword,
            HttpSession session) {

        User user = userService.findUserFromSession(session).orElse(null);
        if (user == null) {
            return new ChangePassword.UserNotFound();
        }
        if (!(user instanceof User.LocalUser localUser)) {
            return new ChangePassword.NotLocalUser();
        }

        if (!passwordEncoder.matches(currentPassword, localUser.password())) {
            return new ChangePassword.IncorrectPassword();
        }

        if (!newPassword.equals(confirmPassword)) {
            return new ChangePassword.PasswordMismatch();
        }

        String hashedPassword = passwordEncoder.encode(newPassword);
        User updatedUser = new User.LocalUser(
                localUser.username(),
                hashedPassword,
                localUser.email(),
                localUser.displayName(),
                localUser.theme()
        );
        userService.save(updatedUser);
        return new ChangePassword.Success(updatedUser);
    }
}