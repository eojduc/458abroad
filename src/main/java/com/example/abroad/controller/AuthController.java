package com.example.abroad.controller;

import com.example.abroad.exception.EmailAlreadyInUseException;
import com.example.abroad.exception.UsernameAlreadyInUseException;
import com.example.abroad.model.Role;
import com.example.abroad.respository.StudentRepository;
import com.example.abroad.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
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

@Controller
public class AuthController {

  private final UserService userService;
  private final AuthenticationManager authenticationManager;


  @Autowired
  public AuthController(UserService userService, AuthenticationManager authenticationManager) {
    this.userService = userService;
    this.authenticationManager = authenticationManager;
  }

  @GetMapping("/login")
  public String showLoginForm() {
    System.out.println("get mapping /login geting called");
    return "auth/login";
  }

  @GetMapping("/register")
  public String showRegistrationForm() {
    System.out.println("get mapping /register geting called");
    return "auth/register";
  }

  @PostMapping("/register")
  public String registerUser(@RequestParam String username,
                             @RequestParam String displayName,
                             @RequestParam String email,
                             @RequestParam String password,
                             HttpServletRequest request,
                             Model model) {
    try {
      userService.registerStudent(username, displayName, email, password);

      // Create authentication token
      UsernamePasswordAuthenticationToken authToken =
              new UsernamePasswordAuthenticationToken(username, password);

      // Authenticate the user
      Authentication authentication = authenticationManager.authenticate(authToken);

      // Set the authentication in the security context
      SecurityContext securityContext = SecurityContextHolder.getContext();
      securityContext.setAuthentication(authentication);

      // Explicitly create the session and store the security context
      HttpSession session = request.getSession(true); //session persist throuhg all http requests
      session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);
      session.setAttribute("username", username);
      session.setAttribute("displayName", displayName);
      session.setAttribute("user", userService.findByUsername(username).orElse(null));
      return "redirect:/";

    } catch (UsernameAlreadyInUseException e) {
      model.addAttribute("error", "Username is already taken");
      return "auth/register";
    } catch (EmailAlreadyInUseException e) {
      model.addAttribute("error", "Email is already registered");
      return "auth/register";
    }
  }

}