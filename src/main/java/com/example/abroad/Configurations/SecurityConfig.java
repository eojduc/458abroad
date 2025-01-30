package com.example.abroad.Configurations;

import com.example.abroad.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
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
      .cors(cors -> cors.disable())
      .csrf(csrf -> csrf.disable())
      .authorizeHttpRequests(auth -> auth
        .requestMatchers("/", "/register", "/login", "/logout", "/images/**", "/public/**")
        .permitAll()
        .requestMatchers("/admin/**").hasRole("ADMIN")
        .anyRequest().authenticated()
      )
      .formLogin(form -> form
        .loginPage("/login")
        .loginProcessingUrl("/login")
        .successHandler(authSuccessHandler) // my custom authentication success handler
        .failureUrl("/login?error=true")
        .permitAll()
      )
      .logout(logout -> logout
        .logoutUrl("/logout")
        .logoutSuccessUrl("/login")
        .permitAll()
      );
    return http.build();
  }


}