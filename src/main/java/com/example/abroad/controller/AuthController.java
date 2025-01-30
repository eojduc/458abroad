package com.example.abroad.controller;

import com.example.abroad.exception.EmailAlreadyInUseException;
import com.example.abroad.exception.UsernameAlreadyInUseException;
import com.example.abroad.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
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

    /*
    @PostMapping("/login")
    public String loginUser(@RequestParam String username,
                            @RequestParam String password,
                            HttpServletRequest request,
                            Model model) {
        System.out.println("post mapping /login geting called");
        try {

            // Create authentication token
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(username, password);

            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(authToken);

            // Set authentication in security context
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Create session and set user
            User user = userService.authenticateUser(username, password);
            userService.setUser(request, user);
            System.out.println("Setting username in session: " + username);
            request.getSession().setAttribute("username", username); //need this in dashboard controller
            //Question for Group: ask if there is a fixed amount of attributes that can be set, and what is the list of these attributes


            System.out.println("login successful");
            // Redirect based on role
            if (user.isAdmin()) {
                return "redirect:/admin/dashboard";
            }
            return "redirect:/dashboard";


        } catch (UsernameNotFoundException e) {
            model.addAttribute("error", "Username not found");
            System.out.println("login failed, Username not found");
            return "auth/login";
        } catch (IncorrectPasswordException | BadCredentialsException e) {
            model.addAttribute("error", "Incorrect password");
            System.out.println("login failed, Incorrect password");
            return "auth/login";
        }
    }


     */


  @GetMapping("/register")
  public String showRegistrationForm() {
    System.out.println("get mapping /register geting called");
    return "auth/register";
  }

  @PostMapping("/register")
  public String registerUser(@RequestParam String username,
    @RequestParam String email,
    @RequestParam String password,
    Model model) {
    System.out.println("post mapping /register geting called");
    try {
      userService.registerStudent(username, email, password);  // Note: changed to registerStudent
      return "redirect:/login?registered=true";
    } catch (UsernameAlreadyInUseException e) {
      model.addAttribute("error", "Username is already taken");
      return "auth/register";
    } catch (EmailAlreadyInUseException e) {
      model.addAttribute("error", "Email is already registered");
      return "auth/register";
    }
  }

}