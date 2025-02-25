package com.example.abroad.configuration;

import com.example.abroad.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final AuthSuccessHandler authSuccessHandler;

  public SecurityConfig(AuthSuccessHandler authSuccessHandler) {
    this.authSuccessHandler = authSuccessHandler;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuthenticationManager authenticationManager(HttpSecurity http,
    CustomUserDetailsService userDetailsService,
    PasswordEncoder passwordEncoder) throws Exception {
    var builder = http.getSharedObject(AuthenticationManagerBuilder.class);
    builder
      .userDetailsService(userDetailsService)
      .passwordEncoder(passwordEncoder);
    return builder.build();
  }


  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
      .csrf(AbstractHttpConfigurer::disable)
      .authorizeHttpRequests(auth -> auth
        .anyRequest()
        .permitAll()
      )
      .formLogin(form -> form
        .loginPage("/login")  // Where users SEE the login form, my defined page
        .loginProcessingUrl("/login")  // Where the form SUBMITS to, spring security intercepts POST requests to /logi
        .successHandler(authSuccessHandler) // my custom authentication success handler
        .failureUrl("/login?error=failed to login")
        .permitAll()
      )
      .logout(AbstractHttpConfigurer::disable);
    return http.build();
  }
}

/*
* Comment on how this works:
* .loginPage("/login")  Where users SEE the login form, my defined page
* .loginProcessingUrl("/login")  Where the form SUBMITS to, spring security filter intercepts POST requests to /login
* The filter creates an Authentication object containing:
* Username from the form
* Password from the form
* List of authorities (initially empty)
* This Authentication object is passed to the AuthenticationManager which:

*Uses my CustomUserDetailsService to load the user by username
*Uses my configured PasswordEncoder to verify the submitted password
* If successful, creates a fully populated Authentication object with the user's authorities/roles
* .successHandler(authSuccessHandler) my custom authentication success handler
*/
