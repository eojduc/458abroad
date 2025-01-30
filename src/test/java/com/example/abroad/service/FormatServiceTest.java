package com.example.abroad.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.abroad.model.Program;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class FormatServiceTest {

  private FormatService service;


  @BeforeEach
  void setUp() {
    service = new FormatService();
  }

  @Test
  void testFormatInstant() {
    var instant = LocalDateTime.parse("2021-09-01T00:00:00").atZone(ZoneId.of("America/New_York"))
      .toInstant();
    var formatted = service.formatInstant(instant);
    assertThat(formatted).isEqualTo("September 01, 2021 at 12:00 AM");
  }

  @Test
  void testFormatLocalDate() {
    var localDate = LocalDate.parse("2021-09-01");
    var formatted = service.formatLocalDate(localDate);
    assertThat(formatted).isEqualTo("September 01, 2021");
  }

  @Test
  void testFormatYear() {
    var year = Year.parse("2021");
    var formatted = service.formatYear(year);
    assertThat(formatted).isEqualTo("2021");
  }

  @Test
  void testFormatSemester() {
    var fall = Program.Semester.FALL;
    var spring = Program.Semester.SPRING;
    var summer = Program.Semester.SUMMER;
    assertThat(service.formatSemester(fall)).isEqualTo("Fall");
    assertThat(service.formatSemester(spring)).isEqualTo("Spring");
    assertThat(service.formatSemester(summer)).isEqualTo("Summer");
  }
}
