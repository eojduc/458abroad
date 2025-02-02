package com.example.abroad.controller;

import com.example.abroad.Configurations.AuthSuccessHandler;
import com.example.abroad.exception.EmailAlreadyInUseException;
import com.example.abroad.exception.UsernameAlreadyInUseException;
import com.example.abroad.model.Role;
import com.example.abroad.respository.StudentRepository;
import com.example.abroad.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;

@Controller
public class AuthController {

  private final UserService userService;
  private final AuthenticationManager authenticationManager;
  private final AuthSuccessHandler authSuccessHandler;


  @Autowired
  public AuthController(UserService userService, AuthenticationManager authenticationManager, AuthSuccessHandler authSuccessHandler) {
    this.userService = userService;
    this.authenticationManager = authenticationManager;
    this.authSuccessHandler = authSuccessHandler;
  }

  @GetMapping("/login")
  public String showLoginForm(HttpServletRequest request, HttpSession session) {
    // Check if user is already authenticated
    if(session.getAttribute("user") != null) {
      System.out.println("in /LOGIN, User is already authenticated, redirecting to home");
      return "redirect:/";
    }
    System.out.println("get mapping /login getting called");
    return "auth/login";
  }

  @GetMapping("/register")
  public String showRegistrationForm(HttpServletRequest request, HttpSession session, HttpServletResponse response) {
    // Check if user is already authenticated
    if(session.getAttribute("user") != null) {
      System.out.println("in /REGISTER, User is already authenticated, redirecting to home");
      return "redirect:/";
    }

    System.out.println("get mapping /register getting called. user not authenticated");
    return "auth/register";
  }

  @PostMapping("/register")
  public String registerUser(@RequestParam String username,
                             @RequestParam String displayName,
                             @RequestParam String email,
                             @RequestParam String password,
                             HttpServletRequest request,
                             HttpServletResponse response,  // Add this parameter
                             Model model) {
    try {

      userService.registerStudent(username, displayName, email, password);

      // Create authentication token
      UsernamePasswordAuthenticationToken authToken =
              new UsernamePasswordAuthenticationToken(username, password);

      // Authenticate the user
      Authentication authentication = authenticationManager.authenticate(authToken);
      SecurityContextHolder.getContext().setAuthentication(authentication);

      //save the session
      HttpSession session = request.getSession(true);
      SecurityContext sc = SecurityContextHolder.getContext();
      session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, sc);

      // Call the authentication success handler directly
      try {
        authSuccessHandler.onAuthenticationSuccess(request, response, authentication);
        return null; // The redirect is handled by onAuthenticationSuccess
      } catch (IOException e) {
        // Handle any IOExceptions from the redirect
        e.printStackTrace();
        return "redirect:/login";
      }

    } catch (UsernameAlreadyInUseException e) {
      model.addAttribute("error", "Username is already taken");
      return "auth/register";
    } catch (EmailAlreadyInUseException e) {
      model.addAttribute("error", "Email is already registered");
      return "auth/register";
    }
  }

}