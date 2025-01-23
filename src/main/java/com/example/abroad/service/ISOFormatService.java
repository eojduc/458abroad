package com.example.abroad.service;

import com.example.abroad.Config;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Service;

@Service
public class ISOFormatService {

  public String formatInstant(Instant instant) {
    return instant.atZone(Config.ZONE_ID) // iso string
      .toLocalDateTime()
      .format(DateTimeFormatter.ISO_DATE_TIME);
  }

}
