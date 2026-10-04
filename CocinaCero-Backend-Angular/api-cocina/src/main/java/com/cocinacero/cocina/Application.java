package com.cocinacero.cocina;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
    exclude =
        org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration
            .class,
    scanBasePackages = {"com.cocinacero.cocina", "com.cocinacero.comun"})
public class Application {
  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }
}
