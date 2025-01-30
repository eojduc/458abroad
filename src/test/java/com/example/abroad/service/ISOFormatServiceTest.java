package com.example.abroad.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ISOFormatServiceTest {

  private final ISOFormatService isoFormatService = new ISOFormatService();

  @Test
  void testFormatInstant() {
    var instant = Instant.parse("2021-08-01T12:34:56-04:00");
    var formatted = isoFormatService.formatInstant(instant);
    assertThat(formatted).isEqualTo("2021-08-01T12:34:56");
  }

}
