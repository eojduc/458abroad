package com.example.abroad;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@SpringBootApplication
@EnableTransactionManagement
public class AbroadApplication {

  public static void main(String[] args) {
    SpringApplication.run(AbroadApplication.class, args);
  }

}
