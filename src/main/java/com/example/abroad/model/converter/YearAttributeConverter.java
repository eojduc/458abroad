package com.example.abroad.model.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.Year;

@Converter(autoApply = true)
public class YearAttributeConverter implements AttributeConverter<Year, Integer> {

  @Override
  public Integer convertToDatabaseColumn(Year attribute) {
    return attribute.getValue();
  }

  @Override
  public Year convertToEntityAttribute(Integer dbData) {
    return Year.of(dbData);
  }
}


