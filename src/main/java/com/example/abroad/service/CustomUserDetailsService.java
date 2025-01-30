package com.example.abroad.service;

import com.example.abroad.model.Admin;
import com.example.abroad.model.Student;
import com.example.abroad.model.User;
import com.example.abroad.respository.AdminRepository;
import com.example.abroad.respository.StudentRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// CustomUserDetailsService.java
@Service
public class CustomUserDetailsService implements UserDetailsService {

  private final StudentRepository studentRepository;
  private final AdminRepository adminRepository;

  public CustomUserDetailsService(StudentRepository studentRepository,
    AdminRepository adminRepository) {
    this.studentRepository = studentRepository;
    this.adminRepository = adminRepository;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    System.out.println("loading use by Username");
    Optional<Admin> admin = adminRepository.findByUsername(username);
    if (admin.isPresent()) {
      return createUserDetails(admin.get());
    }

    Optional<Student> student = studentRepository.findByUsername(username);
    if (student.isPresent()) {
      return createUserDetails(student.get());
    }

    throw new UsernameNotFoundException("User not found: " + username);
  }

  private UserDetails createUserDetails(User user) {
    return new org.springframework.security.core.userdetails.User(
      user.username(),
      user.password(),
      List.of(new SimpleGrantedAuthority(user.role().name()))
    );
  }
}