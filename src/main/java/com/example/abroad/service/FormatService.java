package com.example.abroad.service;

import com.example.abroad.model.Program.Semester;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Year;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Service;

@Service
public record FormatService() {

  public String formatInstant(Instant instant) {
    return instant.atZone(ZoneId.systemDefault())
      .format(DateTimeFormatter.ofPattern("MMMM dd, yyyy 'at' hh:mm a"));
  }

  public String formatLocalDate(LocalDate localDate) {
    return localDate.format(DateTimeFormatter.ofPattern("MMMM dd, yyyy"));
  }

  public String formatYear(Year year) {
    return year.toString();
  }

  public String formatSemester(Semester instant) {
    return switch (instant) {
      case FALL -> "Fall";
      case SPRING -> "Spring";
      case SUMMER -> "Summer";
    };
  }

}
