package com.example.abroad.model;

import java.util.Optional;

public record Alerts(
  Optional<String> error,
  Optional<String> success,
  Optional<String> warning,
  Optional<String> info
) {

}
