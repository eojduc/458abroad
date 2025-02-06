package com.example.abroad.service;
import com.example.abroad.model.Admin;
import com.example.abroad.model.Student;
import com.example.abroad.model.User;
import com.example.abroad.respository.AdminRepository;
import com.example.abroad.respository.StudentRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public record AccountService(
        AdminRepository adminRepository,
        StudentRepository studentRepository,
        PasswordEncoder passwordEncoder,
        UserService userService
) {
    public sealed interface GetProfile permits GetProfile.Success, GetProfile.UserNotFound {
        record Success(User user) implements GetProfile {}
        record UserNotFound() implements GetProfile {}
    }

    public GetProfile getProfile(HttpSession session) {
        User user = userService.getUser(session).orElse(null);
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
        User user = userService.getUser(session).orElse(null);
        if (user == null) {
            return new UpdateProfile.UserNotFound();
        }

        User updatedUser;
        if (user.isAdmin()) {
            Admin admin = (Admin) user;
            updatedUser = new Admin(
                    admin.username(),
                    admin.password(),
                    email,
                    displayName
            );
            adminRepository.save((Admin) updatedUser);
        } else {
            Student student = (Student) user;
            updatedUser = new Student(
                    student.username(),
                    student.password(),
                    email,
                    displayName
            );
            studentRepository.save((Student) updatedUser);
        }

        return new UpdateProfile.Success(updatedUser);
    }

    public sealed interface ChangePassword permits
            ChangePassword.Success,
            ChangePassword.UserNotFound,
            ChangePassword.IncorrectPassword,
            ChangePassword.PasswordMismatch {

        record Success(User updatedUser) implements ChangePassword {}
        record UserNotFound() implements ChangePassword {}
        record IncorrectPassword() implements ChangePassword {}
        record PasswordMismatch() implements ChangePassword {}
    }

    public ChangePassword changePassword(
            String currentPassword,
            String newPassword,
            String confirmPassword,
            HttpSession session) {

        User user = userService.getUser(session).orElse(null);
        if (user == null) {
            return new ChangePassword.UserNotFound();
        }

        if (!passwordEncoder.matches(currentPassword, user.password())) {
            return new ChangePassword.IncorrectPassword();
        }

        if (!newPassword.equals(confirmPassword)) {
            return new ChangePassword.PasswordMismatch();
        }

        String hashedPassword = passwordEncoder.encode(newPassword);
        User updatedUser;

        if (user.isAdmin()) {
            Admin admin = (Admin) user;
            updatedUser = new Admin(
                    admin.username(),
                    hashedPassword,
                    admin.email(),
                    admin.displayName()
            );
            adminRepository.save((Admin) updatedUser);
        } else {
            Student student = (Student) user;
            updatedUser = new Student(
                    student.username(),
                    hashedPassword,
                    student.email(),
                    student.displayName()
            );
            studentRepository.save((Student) updatedUser);
        }

        return new ChangePassword.Success(updatedUser);
    }
}