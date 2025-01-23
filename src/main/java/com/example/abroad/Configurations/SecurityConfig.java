package com.example.abroad.Configurations;
import com.example.abroad.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    public SecurityConfig(CustomUserDetailsService userDetailsService,
                          PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            HttpSecurity http,                    // Provides security configuration context
            UserDetailsService userDetailsService, // Service to load user details from database
            PasswordEncoder passwordEncoder        // Service to hash and verify passwords
    ) throws Exception {

        // Get the authentication builder from Spring's shared objects
        // This builder helps configure how authentication will work
        var builder = http.getSharedObject(AuthenticationManagerBuilder.class);

        // Configure the builder with:
        builder
                // 1. UserDetailsService: tells Spring how to find users
                // When someone tries to log in, Spring will:
                //   - Take their username
                //   - Pass it to UserDetailsService
                //   - UserDetailsService finds user in database
                //   - Returns user's details (username, hashed password, roles)
                .userDetailsService(userDetailsService)

                // 2. PasswordEncoder: tells Spring how to verify passwords
                // When checking a password, Spring will:
                //   - Take the password from login attempt
                //   - Use this encoder to hash it
                //   - Compare hash with stored hash in database
                .passwordEncoder(passwordEncoder);

        // Build and return the configured AuthenticationManager
        // This manager will handle all authentication requests
        // using the configuration we just set up
        return builder.build();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())  // Be careful with this in production
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/register", "/login", "/logout", "/public/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .permitAll()
                        .disable()  // Disable Spring's auto-configuration for login
                );

        return http.build();
    }
}