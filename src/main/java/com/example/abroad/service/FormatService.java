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
  public String formatTerm(Semester semester, Year year) {
    return formatSemester(semester) + " " + formatYear(year);
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
  public String formatDateRange(LocalDate start, LocalDate end) {
    DateTimeFormatter sameMonthFormatter = DateTimeFormatter.ofPattern("MMM d");
    DateTimeFormatter differentMonthFormatter = DateTimeFormatter.ofPattern("MMM d");

    if (start.getMonth() == end.getMonth()) {
      return start.format(sameMonthFormatter) + " - " + end.format(DateTimeFormatter.ofPattern("d"));
    } else {
      return start.format(differentMonthFormatter) + " - " + end.format(differentMonthFormatter);
    }
  }

  public String formatInstantToIso(Instant instant) {
    return instant.atZone(ZoneId.systemDefault())
      .toLocalDateTime()
      .format(DateTimeFormatter.ISO_DATE_TIME);
  }

  public String abbreviateName(String name) {
    String[] names = name.split(" ");
    StringBuilder sb = new StringBuilder();
    for (String n : names) {
      sb.append(n.charAt(0));
    }
    return sb.toString().trim();
  }

}
