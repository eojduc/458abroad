package com.example.abroad.service;

import com.example.abroad.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public record ULinkService(UserService userService, TranscriptService transcriptService) {
  private static final Logger log = LoggerFactory.getLogger(ULinkService.class);

  public sealed interface SetULink {
    record Success(User updatedUser) implements SetULink {}
    record IncorrectPin() implements SetULink {}
    record UsernameInUse() implements SetULink {}
    record TranscriptServiceError() implements SetULink {}
  }

  public sealed interface RefreshCourses {
    record Success() implements RefreshCourses {}
    record TranscriptServiceError() implements RefreshCourses {}
  }

  public SetULink setULink(User user, String uLinkName, String pin) {
    var existingUser = userService.findAll().stream()
      .anyMatch(u -> uLinkName.equals(u.uLink()));
    if (existingUser) {
      return new SetULink.UsernameInUse();
    }
    try {
      if (!transcriptService.authenticate(uLinkName, pin)) {
        return new SetULink.IncorrectPin();
      }
      var updatedUser = userService.save(user.withULink(uLinkName));
      refreshCourses(updatedUser);
      return new SetULink.Success(updatedUser);
    } catch (Exception e) {
      return new SetULink.TranscriptServiceError();
    }
  }

  public RefreshCourses refreshCourses(User user) {
    try {
      var courseInfos = transcriptService.retrieveTranscriptData(user.uLink());
      for (var courseInfo : courseInfos) {
        userService.saveCourse(user.username(), courseInfo.courseCode(), courseInfo.grade());
      }
      return new RefreshCourses.Success();
    } catch (Exception e) {
      log.error("Error retrieving transcript data: " + e.getMessage());
      return new RefreshCourses.TranscriptServiceError();
    }
  }

}
