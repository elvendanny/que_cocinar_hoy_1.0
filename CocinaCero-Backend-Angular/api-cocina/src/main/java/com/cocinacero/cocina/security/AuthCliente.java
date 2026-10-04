package com.cocinacero.cocina.security;

import com.cocinacero.comun.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.*;
import java.net.http.*;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AuthCliente {
  private final String url, key;
  private final ObjectMapper mapper;
  private final HttpClient http =
      HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();

  public AuthCliente(
      @Value("${app.auth-url}") String u, @Value("${app.internal-key}") String k, ObjectMapper m) {
    url = u;
    key = k;
    mapper = m;
    if (k.length() < 32)
      throw new IllegalStateException("Configura app.internal-key con al menos 32 caracteres.");
  }

  public Actor verificar(String header) {
    if (header == null || !header.startsWith("Bearer "))
      throw new ApiError(401, "Se requiere Bearer Token.");
    try {
      var r =
          http.send(
              HttpRequest.newBuilder(URI.create(url + "/api/internal/verificar"))
                  .timeout(Duration.ofSeconds(5))
                  .header("Authorization", header)
                  .header("X-Internal-Key", key)
                  .GET()
                  .build(),
              HttpResponse.BodyHandlers.ofString());
      if (r.statusCode() == 401 || r.statusCode() == 403)
        throw new ApiError(401, "Token inválido, revocado o cuenta no aprobada.");
      if (r.statusCode() != 200)
        throw new ApiError(503, "Servicio de autenticación no disponible.");
      return mapper.readValue(r.body(), Actor.class);
    } catch (ApiError e) {
      throw e;
    } catch (Exception e) {
      if (e instanceof InterruptedException) Thread.currentThread().interrupt();
      throw new ApiError(503, "Servicio de autenticación no disponible.");
    }
  }
}
