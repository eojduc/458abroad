package com.example.abroad.service;
import com.example.abroad.model.Program;
import com.example.abroad.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class EmailService {

  @Value("${mailgun.api.key}")
  private String mailgunApiKey;

  @Value("${mailgun.domain}")
  private String mailgunDomain;

  @Value("${redirect.url}")
  private String redirectUrl;

  private final RestTemplate restTemplate = new RestTemplate();

  public void sendEmail(String to, String subject, String text, String html) {
    String url = "https://api.mailgun.net/v3/" + mailgunDomain + "/messages";

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    String auth = "api:" + mailgunApiKey;
    byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes(StandardCharsets.UTF_8));
    headers.set("Authorization", "Basic " + new String(encodedAuth));

    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("from", "Your Name <mailgun@" + mailgunDomain + ">");
    body.add("to", to);
    body.add("subject", subject);
    body.add("text", text);
    body.add("html", html); // 💡 Add HTML version here

    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
    System.out.println(text);
    restTemplate.postForEntity(url, request, String.class);
  }



  public void sendRequestEmail(String email, String name, Program program, User student, Integer code) {
    String subject = "Recommendation Request for " + program.title();
    String link = redirectUrl + "/rec-request/" + code;

    String text = "Dear " + name + ",\n\n"
      + "You have been requested to submit a letter of recommendation for " + student.displayName()
      + " for the " + program.title() + " program.\n\n"
      + "Please visit the following link to submit your recommendation:\n"
      + link + "\n\n"
      + "Thank you,\n"
      + "HCC Abroad";

    String html = "<p>Dear " + name + ",</p>"
      + "<p>You have been requested to submit a letter of recommendation for <strong>" + student.displayName() + "</strong>"
      + " for the <strong>" + program.title() + "</strong> program.</p>"
      + "<p>Please visit the following link to submit your recommendation:</p>"
      + "<p><a href=\"" + link + "\">Submit Recommendation</a></p>"
      + "<p>Thank you,<br>HCC Abroad</p>";
    System.out.println(html);
    sendEmail(email, subject, text, html);
  }


  public void sendCancelRequestEmail(String email, String name, Program program, User student) {
    String subject = "Recommendation Request Cancelled for " + program.title();

    String text = "Dear " + name + ",\n\n"
      + "The recommendation request for " + student.displayName()
      + " for the " + program.title() + " program has been cancelled. No further action from you is required.\n\n"
      + "Thank you,\n"
      + "HCC Abroad";

    String html = "<p>Dear " + name + ",</p>"
      + "<p>The recommendation request for <strong>" + student.displayName() + "</strong>"
      + " for the <strong>" + program.title() + "</strong> program has been cancelled. "
      + "No further action from you is required.</p>"
      + "<p>Thank you,<br>HCC Abroad</p>";

    sendEmail(email, subject, text, html);
  }

}
