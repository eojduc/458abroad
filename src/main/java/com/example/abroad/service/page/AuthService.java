package com.example.abroad.service.page;

import com.example.abroad.configuration.AuthSuccessHandler;
import com.example.abroad.exception.EmailAlreadyInUseException;
import com.example.abroad.exception.UsernameAlreadyInUseException;
import com.example.abroad.model.User;
import com.example.abroad.model.User.Theme;
import com.example.abroad.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public record AuthService(
        UserService userService,
        AuthenticationManager authenticationManager,
        AuthSuccessHandler authSuccessHandler
) {
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

    public sealed interface RegisterResult permits
            RegisterResult.Success,
            RegisterResult.UsernameExists,
            RegisterResult.EmailExists,
            RegisterResult.AuthenticationError {

        record Success(Authentication authentication) implements RegisterResult {}
        record UsernameExists() implements RegisterResult {}
        record EmailExists() implements RegisterResult {}
        record AuthenticationError(Exception error) implements RegisterResult {}
    }

    public RegisterResult registerUser(
            String username,
            String displayName,
            String email,
            String password,
            HttpServletRequest request) {

        try {
            userService.save(new User.LocalUser(username, password, email, User.Role.STUDENT, displayName, Theme.DEFAULT));

            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(username, password);

            Authentication authentication = authenticationManager.authenticate(authToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            HttpSession session = request.getSession(true);
            SecurityContext sc = SecurityContextHolder.getContext();
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, sc);

            return new RegisterResult.Success(authentication);

        } catch (UsernameAlreadyInUseException e) {
            return new RegisterResult.UsernameExists();
        } catch (EmailAlreadyInUseException e) {
            return new RegisterResult.EmailExists();
        } catch (Exception e) {
            return new RegisterResult.AuthenticationError(e);
        }
    }

    public void handleSuccessfulAuthentication(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {
        authSuccessHandler.onAuthenticationSuccess(request, response, authentication);
    }
}
