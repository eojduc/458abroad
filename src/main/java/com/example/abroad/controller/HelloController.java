package com.example.abroad.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HelloController {


  @GetMapping("/")
  public String helloWorld(HttpServletRequest request, Model model) {
    var name = Arrays.stream(Optional.ofNullable(request.getCookies()).orElse(new Cookie[0]))
      .filter(cookie -> cookie.getName().equals("name"))
      .findFirst()
      .map(Cookie::getValue)
      .orElse("worldd");
    model.addAttribute("name", name);
    return "hello";
  }



  @PostMapping("/")
  public String helloWorldPost(HttpServletResponse response, @RequestParam String name, Model model) {
    response.addCookie(new Cookie("name", name));
    model.addAttribute("name", name);
    return "hello";
  }
  
  // these are the ones that use htmx
  @GetMapping("/hello")
  public String helloWorld2(HttpServletRequest request, Model model) {
    var name = Arrays.stream(request.getCookies())
      .filter(cookie -> cookie.getName().equals("name"))
      .findFirst()
      .map(Cookie::getValue)
      .orElse("world");
    model.addAttribute("name", name);
    return "hello2";
  }


  @PostMapping("/hello")
  public String helloWorldPost2(HttpServletResponse response, @RequestParam String name, Model model) {
    response.addCookie(new Cookie("name", name));
    model.addAttribute("name", name);
    return "components/hello-card2";
  }

}
