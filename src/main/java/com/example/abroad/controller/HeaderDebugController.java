package com.example.abroad.controller;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class HeaderDebugController {
    @GetMapping("/debug/headers")
    public Map<String, String> headers(HttpServletRequest request) {
        return Collections.list(request.getHeaderNames())
            .stream()
            .collect(Collectors.toMap(
                name -> name,
                request::getHeader
            ));
    }
}