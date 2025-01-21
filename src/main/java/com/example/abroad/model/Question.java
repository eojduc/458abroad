package com.example.abroad.model;

import java.util.List;

public record Question(String field, String text) {
  public static List<Question> QUESTIONS = List.of(
    new Question("answer1", "Why do you want to participate in this study abroad program?"),
    new Question("answer2", "How does this program align with your academic or career goals?"),
    new Question("answer3",
      "What challenges do you anticipate during this experience, and how will you address them?"),
    new Question("answer4", "Describe a time you adapted to a new or unfamiliar environment."),
    new Question("answer5", "What unique perspective or contribution will you bring to the group?")
  );
}
