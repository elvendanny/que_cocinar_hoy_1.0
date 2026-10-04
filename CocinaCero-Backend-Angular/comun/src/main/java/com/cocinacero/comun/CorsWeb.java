package com.cocinacero.comun;

import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/** Orígenes del navegador. La clave interna nunca se entrega a Angular. */
@Configuration
public class CorsWeb {
  @Bean
  UrlBasedCorsConfigurationSource corsConfigurationSource(
      @Value("${app.cors.allowed-origins:http://localhost:4200,http://127.0.0.1:4200}") String origins) {
    var config = new CorsConfiguration();
    var allowed = Arrays.stream(origins.split(",")).map(String::trim).filter(s -> !s.isEmpty()).toList();
    if (allowed.stream().anyMatch(s -> s.contains("*")))
      throw new IllegalArgumentException("CORS requiere orígenes explícitos, sin comodines.");
    config.setAllowedOrigins(allowed);
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
    config.setExposedHeaders(List.of("Content-Disposition"));
    config.setAllowCredentials(false);
    config.setMaxAge(3600L);
    var source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/internal/**", new CorsConfiguration());
    source.registerCorsConfiguration("/api/**", config);
    source.registerCorsConfiguration("/health", config);
    return source;
  }
}
