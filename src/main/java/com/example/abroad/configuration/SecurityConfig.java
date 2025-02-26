package com.example.abroad.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

<<<<<<< Updated upstream
  private final AuthSuccessHandler authSuccessHandler;
  private final ShibbolethSSOFilter shibbolethSSOFilter;

  public SecurityConfig(AuthSuccessHandler authSuccessHandler, ShibbolethSSOFilter shibbolethSSOFilter) {
    this.authSuccessHandler = authSuccessHandler;
    this.shibbolethSSOFilter = shibbolethSSOFilter;
  }

=======
>>>>>>> Stashed changes
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
<<<<<<< Updated upstream
      .csrf(csrf -> csrf.disable())
      // .addFilterBefore(shibbolethSSOFilter, UsernamePasswordAuthenticationFilter.class)
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
=======
      .csrf(AbstractHttpConfigurer::disable)
      // .addFilterBefore(ssoFilter, UsernamePasswordAuthenticationFilter.class)
      .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
>>>>>>> Stashed changes
    return http.build();
  }
}
