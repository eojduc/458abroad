package com.example.abroad.view;

import com.example.abroad.model.Application;

public record Badge(String color, String text) {

  public static Badge fromStatus(String status) {
    return switch (status) {
      case "APPROVED" -> new Badge("success", "Approved");
      case "ENROLLED" -> new Badge("success", "Enrolled");
      case "COMPLETED" -> new Badge("success", "Completed");
      case "APPLIED" -> new Badge("info", "Applied");
      case "ELIGIBLE" -> new Badge("info", "Eligible");
      case "CANCELLED" -> new Badge("error", "Cancelled");
      case "WITHDRAWN" -> new Badge("warning", "Withdrawn");
      default -> new Badge("neutral", "Unknown");
    };
  }

}
