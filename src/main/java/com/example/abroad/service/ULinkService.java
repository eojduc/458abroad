package com.example.abroad.service;

import com.example.abroad.model.User;
import org.springframework.stereotype.Service;

@Service
public record ULinkService(UserService userService, TranscriptService transcriptService) {



  public sealed interface SetULink {
    record Success() implements SetULink {}
    record IncorrectPin() implements SetULink {}
    record UsernameInUse() implements SetULink {}
    record TranscriptServiceError() implements SetULink {}
  }

  public sealed interface RefreshCourses {
    record Success() implements RefreshCourses {}
    record TranscriptServiceError() implements RefreshCourses {}
  }

  public SetULink setULink(User.LocalUser user, String uLinkName, String pin) {
    var existingUser = userService.findAll().stream()
      .anyMatch(u -> uLinkName.equals(u.uLink()));
    if (existingUser) {
      return new SetULink.UsernameInUse();
    }
    try {
      var actualPin = transcriptService.getUserPin(uLinkName);
      if (!actualPin.equals(pin)) {
        return new SetULink.IncorrectPin();
      }
      var updatedUser = new User.LocalUser(
        user.username(),
        user.password(),
        user.email(),
        user.displayName(),
        user.theme(),
        uLinkName
      );
      userService.save(updatedUser);
      return new SetULink.Success();
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
      return new RefreshCourses.TranscriptServiceError();
    }
  }

}
