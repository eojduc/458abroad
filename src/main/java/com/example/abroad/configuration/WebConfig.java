package com.example.abroad.configuration;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;  // Add this import
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Add resource handlers to serve static images.
 *
 * <p>This method configures Spring MVC to serve static images from the
 * classpath. The images are located in the "static/images" directory and can be accessed via the
 * "/images/**" URL pattern.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

  public static class CacheControlInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
      // Set cache control headers
      response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate, max-age=0, post-check=0, pre-check=0");
      response.setHeader("Pragma", "no-cache");
      response.setHeader("Expires", "0");
      return true;
    }
  }

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/images/**")
            .addResourceLocations("classpath:/static/images/");
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(new CacheControlInterceptor())
            .addPathPatterns("/login", "/register"); //prevent caching /login and /register
  }
}