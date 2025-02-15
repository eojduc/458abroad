package com.example.abroad.configuration;

import com.example.abroad.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public record AuthSuccessHandler(UserService userService) implements AuthenticationSuccessHandler {

                                 //controls what is done after successfuly login
                                 @Override
  public void onAuthenticationSuccess(HttpServletRequest request,
                                 HttpServletResponse response,
                                 Authentication authentication) throws IOException {
    userService.saveUserToSession(userService.findByUsername(authentication.getName()).get(), request.getSession());
    response.sendRedirect("/");
  }
}