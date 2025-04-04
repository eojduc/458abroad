package com.example.abroad.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

@Service
public class ULinkTranscriptService implements TranscriptService {

  public static List<CourseInfo> parseTranscript(String html) throws TranscriptServiceException {
    // We'll hold the parsed course info here
    List<CourseInfo> courses = new ArrayList<>();

    // 1. Parse the HTML with Jsoup
    Document doc = Jsoup.parse(html);

    // 2. Grab the <pre> block that contains the transcript lines
    Element preBlock = doc.selectFirst("pre");
    if (preBlock == null) {
      // Throw an exception rather than returning
      throw new TranscriptServiceException("No <pre> block found in the provided HTML.");
    }

    // 3. Split the <pre> text into lines
    String[] lines = preBlock.text().split("\n");

    String currentSemester = null;

    // 4. Process each line
    for (String line : lines) {
      line = line.trim();

      // Skip empty lines (that’s not necessarily an error).
      if (line.isEmpty()) {
        continue;
      }

      // Detect a semester header: lines like "**** FALL 2023 ****"
      if (line.startsWith("****") && line.endsWith("****")) {
        // Extract the semester portion between the asterisks
        // e.g. "**** FALL 2023 ****" -> "FALL 2023"
        currentSemester = line
          .replaceAll("\\*", "") // remove asterisks
          .trim();
        continue;
      }

      // At this point, it's expected to be a valid course line
      // Typically something like:
      //    CODE        COURSE NAME             GRADE
      //    "CHEM 101   INTRO CHEMISTRY         A"
      String[] parts = line.split("\\s{2,}"); // split on runs of 2+ spaces

      // If we can’t split into at least 3 parts, it’s not valid
      if (parts.length < 3) {
        throw new TranscriptServiceException("Invalid course line (must have at least 3 columns): " + line);
      }

      // We also expect a semester header to have been read before any course lines
      if (currentSemester == null) {
        throw new TranscriptServiceException("Encountered a course line before any semester heading: " + line);
      }

      // The first part is the course code, the last part is the grade
      String courseCode = parts[0];
      String grade = parts[parts.length - 1];

      // Combine anything in between as the course name
      StringBuilder nameBuilder = new StringBuilder();
      for (int i = 1; i < parts.length - 1; i++) {
        if (nameBuilder.length() > 0) {
          nameBuilder.append(" ");
        }
        nameBuilder.append(parts[i]);
      }
      String courseName = nameBuilder.toString();

      // Create the CourseInfo record (or class)
      CourseInfo record = new CourseInfo(
        currentSemester,
        courseCode,
        courseName,
        grade
      );
      courses.add(record);
    }

    return courses;
  }
  @Override
  public List<CourseInfo> retrieveTranscriptData(String student) throws TranscriptServiceException {
    try {
      String username = "abroad";
      String password = "ece@458";
      String auth = username + ":" + password;
      String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
      String authHeader = "Basic " + encodedAuth;

      HttpClient client = HttpClient.newHttpClient();
      HttpRequest request = HttpRequest.newBuilder()
        .uri(new URI(
          String.format("http://ulink.colab.duke.edu:8000/cgi-bin/view-schedule.pl?username=%s",
            student)))
        .header("Authorization", authHeader)
        .GET()
        .build();
      var response = client.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() != 200) {
        throw new TranscriptServiceException("Failed to retrieve transcript data");
      }
      String html = response.body();
      return parseTranscript(html);
    } catch (Exception e) {
      throw new TranscriptServiceException("Failed to retrieve transcript data: " + e.getMessage());
    }
  }

  @Override
  public String getUserPin(String student) throws TranscriptServiceException {
    try {
      String username = "abroad";
      String password = "ece@458";
      String auth = username + ":" + password;
      String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
      String authHeader = "Basic " + encodedAuth;

      HttpClient client = HttpClient.newHttpClient();
      HttpRequest request = HttpRequest.newBuilder()
        .uri(new URI(
          String.format("http://ulink.colab.duke.edu:8000/cgi-bin/view-pin.pl?username=%s",
            student)))
        .header("Authorization", authHeader)
        .GET()
        .build();
      var response = client.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() != 200) {
        throw new TranscriptServiceException("Failed to retrieve pin");
      }
      String html = response.body();
      return extractPin(html);
    } catch (Exception e) {
      throw new TranscriptServiceException("Failed to retrieve user pin: " + e.getMessage());
    }
  }
  public static String extractPin(String html) throws TranscriptServiceException {
    // Parse the HTML
    Document doc = Jsoup.parse(html);

    // Find the <td> that contains "PIN for"
    // Then select the <b> tag inside it.
    Element pinBold = doc.selectFirst("td:contains(PIN for) b");
    if (pinBold != null) {
      // Return the text inside the <b> tag (e.g. "8344")
      return pinBold.text();
    }

    // If not found, return null (or throw an exception, etc.)
    throw new TranscriptServiceException("PIN not found in the HTML");
  }

}
