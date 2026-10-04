package com.cocinacero.cocina;

import static org.junit.jupiter.api.Assertions.*;

import com.cocinacero.auth.service.CorreoService;
import com.fasterxml.jackson.databind.*;
import java.net.*;
import java.net.http.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * Dos servidores HTTP reales y dos bases H2 independientes. Solo Gmail se sustituye por una captura
 * local de prueba.
 */
class MicroserviciosIntegrationTest {
  static final AtomicReference<String> codigo = new AtomicReference<>();

  @TestConfiguration
  static class MailTest {
    @Bean
    @Primary
    CorreoService correoPrueba() {
      return (e, c) -> codigo.set(c);
    }
  }

  ObjectMapper json = new ObjectMapper();
  HttpClient http = HttpClient.newHttpClient();

  JsonNode call(String base, String path, String method, Object body, String token, int status)
      throws Exception {
    var b = HttpRequest.newBuilder(URI.create(base + path));
    if (token != null) b.header("Authorization", "Bearer " + token);
    if (body != null) b.header("Content-Type", "application/json");
    b.method(
        method,
        body == null
            ? HttpRequest.BodyPublishers.noBody()
            : HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body)));
    var r = http.send(b.build(), HttpResponse.BodyHandlers.ofString());
    assertEquals(status, r.statusCode(), r.body());
    return r.body().isBlank() ? json.nullNode() : json.readTree(r.body());
  }

  @Test
  void tokenRealCruzaServiciosYBloqueoRevoca() throws Exception {
    try (var auth =
        new SpringApplicationBuilder(com.cocinacero.auth.Application.class, MailTest.class)
            .run(
                "--spring.config.import=",
                "--server.port=0",
                "--spring.datasource.url=jdbc:h2:mem:httpauth;MODE=MariaDB;DB_CLOSE_DELAY=-1",
                "--spring.datasource.driver-class-name=org.h2.Driver",
                "--spring.datasource.username=sa",
                "--spring.datasource.password=",
                "--spring.jpa.hibernate.ddl-auto=create-drop",
                "--app.jwt-secret=http-test-jwt-secret-abcdefghijklmnopqrstuvwxyz",
                "--app.internal-key=http-internal-test-key-abcdefghijklmnopqrstuvwxyz",
                "--app.admin-email=admin@http.local",
                "--app.admin-password=AdminHttp2026!",
                "--app.gmail-token=target/no-token.json",
                "--app.gmail-from=test@example.com")) {
      String authUrl =
          "http://localhost:"
              + ((ServletWebServerApplicationContext) auth).getWebServer().getPort();
      try (var cocina =
          new SpringApplicationBuilder(com.cocinacero.cocina.Application.class)
              .run(
                  "--spring.config.import=",
                  "--server.port=0",
                  "--spring.datasource.url=jdbc:h2:mem:httpcocina;MODE=MariaDB;DB_CLOSE_DELAY=-1",
                  "--spring.datasource.driver-class-name=org.h2.Driver",
                  "--spring.datasource.username=sa",
                  "--spring.datasource.password=",
                  "--spring.jpa.hibernate.ddl-auto=create-drop",
                  "--app.internal-key=http-internal-test-key-abcdefghijklmnopqrstuvwxyz",
                  "--app.auth-url=" + authUrl,
                  "--app.uploads=target/http-test-files")) {
        String cookUrl =
            "http://localhost:"
                + ((ServletWebServerApplicationContext) cocina).getWebServer().getPort();
        call(cookUrl, "/api/recetas", "GET", null, null, 401);
        var reg =
            call(
                authUrl,
                "/api/auth/registro",
                "POST",
                Map.of(
                    "nombre",
                    "Usuario HTTP",
                    "email",
                    "user@http.local",
                    "password",
                    "UsuarioHttp2026!"),
                null,
                201);
        long id = reg.path("usuario").path("id").asLong();
        call(
            authUrl,
            "/api/auth/verificar",
            "POST",
            Map.of("email", "user@http.local", "codigo", codigo.get()),
            null,
            200);
        call(
            authUrl,
            "/api/auth/login",
            "POST",
            Map.of("email", "user@http.local", "password", "UsuarioHttp2026!"),
            null,
            403);
        String admin =
            call(
                    authUrl,
                    "/api/auth/login",
                    "POST",
                    Map.of("email", "admin@http.local", "password", "AdminHttp2026!"),
                    null,
                    200)
                .path("token")
                .asText();
        call(
            authUrl,
            "/api/usuarios/" + id + "/estado",
            "PATCH",
            Map.of("estado", "APROBADA", "motivo", "Revisión HTTP"),
            admin,
            200);
        String user =
            call(
                    authUrl,
                    "/api/auth/login",
                    "POST",
                    Map.of("email", "user@http.local", "password", "UsuarioHttp2026!"),
                    null,
                    200)
                .path("token")
                .asText();
        call(cookUrl, "/api/recetas", "GET", null, user, 200);
        call(cookUrl, "/api/categorias", "POST", Map.of("nombre", "Categoria HTTP"), user, 403);
        call(cookUrl, "/api/categorias", "POST", Map.of("nombre", "Categoria HTTP"), admin, 201);
        call(
            authUrl,
            "/api/usuarios/" + id + "/estado",
            "PATCH",
            Map.of("estado", "BLOQUEADA", "motivo", "Bloqueo HTTP"),
            admin,
            200);
        call(cookUrl, "/api/recetas", "GET", null, user, 401);
        call(authUrl, "/api/auth/logout", "POST", null, admin, 200);
        call(cookUrl, "/api/recetas", "GET", null, admin, 401);
      }
    }
  }
}
