package com.example.abroad.service;

import com.example.abroad.model.Program;
import com.example.abroad.model.RebrandConfig;
import com.example.abroad.model.ThemeConfig;
import com.example.abroad.model.User.Theme;
import com.example.abroad.respository.PreReqRepository;
import com.example.abroad.respository.ThemeConfigRepository;
import com.example.abroad.service.ProgramService.SaveProgram;
import java.util.Map;
import org.springframework.stereotype.Service;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;

@Service
public record PrereqService(PreReqRepository preReqRepository) {

  public ParsePrereq normalizeCourse(String courseInput) {
    // Handle null or empty input
    if (courseInput == null || courseInput.trim().isBlank()) {
      return new ParsePrereq.InvalidInput("Course input cannot be null or empty");
    }

    // Normalize whitespace and convert to uppercase
    String normalized = courseInput.trim().toUpperCase();

    // Handle potential multi-whitespace between department and number
    normalized = normalized.replaceAll("\\s+", " ");

    // Validate using the required regex pattern
    if (!normalized.matches("[A-Z0-9]{1,8} \\d{3}")) {
      return new ParsePrereq.InvalidInput("Error: %s - Course must be in format 'DEPT 123' where department is 1-8 characters".formatted(courseInput));
    }

    return new ParsePrereq.Success(normalized);
  }



  public sealed interface ParsePrereq {
    record Success(String normalizedCourse) implements PrereqService.ParsePrereq {}
    record InvalidInput(String message) implements PrereqService.ParsePrereq {}
  }

}
