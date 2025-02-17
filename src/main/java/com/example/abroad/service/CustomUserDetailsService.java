package com.example.abroad.service;

import com.example.abroad.model.User;
import java.util.List;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// CustomUserDetailsService.java
@Service
public record CustomUserDetailsService(UserService userService) implements UserDetailsService {

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    var user = userService.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    if (!(user instanceof User.LocalUser localUser)) {
      throw new UsernameNotFoundException("User not found: " + username);
    }
    return createUserDetails(localUser);
  }

  private UserDetails createUserDetails(User.LocalUser user) {
    return new org.springframework.security.core.userdetails.User(
      user.username(),
      user.password(),
      List.of()
    );
  }
}