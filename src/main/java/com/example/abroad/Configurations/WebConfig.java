package com.example.abroad.Configurations;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
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

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry.addResourceHandler("/images/**")
      .addResourceLocations("classpath:/static/images/");
  }
}