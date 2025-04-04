package com.example.abroad.service;


import java.util.List;

public interface TranscriptService {

  record CourseInfo(
    String semester,
    String courseCode,
    String courseName,
    String grade
  ) {}

  class TranscriptServiceException extends Exception {
    public TranscriptServiceException(String message) {
      super(message);
    }
  }

  List<CourseInfo> retrieveTranscriptData(String student) throws TranscriptServiceException;

  String getUserPin(String student) throws TranscriptServiceException;
}
