package com.example.abroad.controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MultipartException;
import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class FileUploadExceptionAdvice {

    @ExceptionHandler(MultipartException.class)
    public String handleMaxSizeException(MultipartException exc, HttpServletRequest request) {
        // Extract the application ID from the request path
        String path = request.getRequestURI();
        String[] segments = path.split("/");
        String applicationId = "";
        for (int i = 0; i < segments.length; i++) {
            if (segments[i].equals("applications") && i + 1 < segments.length) {
                applicationId = segments[i + 1];
                break;
            }
        }

        return "redirect:/applications/" + applicationId + "?error=File is too large. Maximum allowed size is 10MB#documents";
    }
}
