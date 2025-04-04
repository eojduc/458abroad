package com.example.abroad.service;


import java.util.List;

/**
 * Service interface for retrieving transcript data from an external service.
 * Also contains methods for authentication with the external service.
 */
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
  /**
   * Retrieves the transcript data for a given student.
   *
   * @param student The student's username.
   * @return A list of CourseInfo records containing the transcript data.
   * @throws TranscriptServiceException If an error occurs while retrieving the data.
   */
  List<CourseInfo> retrieveTranscriptData(String student) throws TranscriptServiceException;

  /**
   * Authenticates a student with the external service using their username and PIN.
   *
   * @param username The student's username.
   * @param pin      The student's PIN.
   * @return true if authentication is successful, false otherwise.
   * @throws TranscriptServiceException If an error occurs during authentication.
   */
  Boolean authenticate(String username, String pin) throws TranscriptServiceException;
}
