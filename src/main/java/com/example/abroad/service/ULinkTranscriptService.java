package com.example.abroad.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

@Service
public class ULinkTranscriptService implements TranscriptService {

  public static List<CourseInfo> parseTranscript(String html) {
    List<CourseInfo> courses = new ArrayList<>();

    // 1. Parse the HTML with Jsoup
    Document doc = Jsoup.parse(html);

    // 2. Grab the <pre> block that contains the transcript lines
    Element preBlock = doc.selectFirst("pre");
    if (preBlock == null) {
      // No <pre> found - return empty list
      return courses;
    }

    // 3. Split the <pre> text into lines
    String[] lines = preBlock.text().split("\n");

    String currentSemester = null;

    // 4. Process each line
    for (String line : lines) {
      line = line.trim();

      // Skip empty lines
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

      // Attempt to parse as a course line. Usually in the form:
      //   CODE (e.g. "CHEM 101")   TITLE (could be multiple words)   GRADE (e.g. "A", "B+", "IP", etc.)
      // We'll use split on 2+ spaces. The final piece is grade, the first is course code, the middle is the name.
      String[] parts = line.split("\\s{2,}"); // split on runs of 2+ spaces

      // We expect at least 3 parts if it's a valid course line
      if (parts.length >= 3) {
        String courseCode = parts[0];
        String grade = parts[parts.length - 1];

        // Everything between parts[1] and parts[parts.length - 2] is the course name
        // But if you used split("\\s{2,}"), in these examples it typically yields exactly 3 parts:
        //   parts[0] -> code
        //   parts[1] -> course name
        //   parts[2] -> grade
        // If there's more, join the middle parts for the course name.
        StringBuilder nameBuilder = new StringBuilder();
        for (int i = 1; i < parts.length - 1; i++) {
          if (nameBuilder.length() > 0) {
            nameBuilder.append(" ");
          }
          nameBuilder.append(parts[i]);
        }
        String courseName = nameBuilder.toString();

        // Create the record object
        var record = new CourseInfo(
          currentSemester == null ? "" : currentSemester,
          courseCode,
          courseName,
          grade
        );
        courses.add(record);
      }
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
  public Boolean authenticate(String student, String pin) throws TranscriptServiceException {
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
      return extractPin(html).equals(pin);
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
