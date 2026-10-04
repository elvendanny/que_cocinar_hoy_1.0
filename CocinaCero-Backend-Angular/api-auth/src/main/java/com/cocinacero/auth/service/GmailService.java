package com.cocinacero.auth.service;

import com.cocinacero.comun.ApiError;
import com.fasterxml.jackson.databind.*;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class GmailService implements CorreoService {
  private final ObjectMapper mapper;
  private final String tokenPath, from;
  private final HttpClient http =
      HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

  public GmailService(
      ObjectMapper mapper,
      @Value("${app.gmail-token}") String tokenPath,
      @Value("${app.gmail-from}") String from) {
    this.mapper = mapper;
    this.tokenPath = tokenPath;
    this.from = from;
  }

  private String enc(String s) {
    return URLEncoder.encode(s, StandardCharsets.UTF_8);
  }

  public void enviar(String to, String codigo) {
    try {
      if (from.isBlank()
          || to.contains("\r")
          || to.contains("\n")
          || from.contains("\r")
          || from.contains("\n")) throw new IllegalStateException();
      JsonNode token = mapper.readTree(Files.readString(Path.of(tokenPath)));
      for (String k : List.of("client_id", "client_secret", "refresh_token"))
        if (!token.hasNonNull(k) || token.path(k).asText().isBlank())
          throw new IllegalStateException();
      String form =
          "grant_type=refresh_token&client_id="
              + enc(token.path("client_id").asText())
              + "&client_secret="
              + enc(token.path("client_secret").asText())
              + "&refresh_token="
              + enc(token.path("refresh_token").asText());
      HttpResponse<String> auth =
          http.send(
              HttpRequest.newBuilder(URI.create("https://oauth2.googleapis.com/token"))
                  .timeout(Duration.ofSeconds(20))
                  .header("Content-Type", "application/x-www-form-urlencoded")
                  .POST(HttpRequest.BodyPublishers.ofString(form))
                  .build(),
              HttpResponse.BodyHandlers.ofString());
      if (auth.statusCode() != 200) throw new IllegalStateException();
      String access = mapper.readTree(auth.body()).path("access_token").asText();
      String body =
          "From: "
              + from
              + "\r\nTo: "
              + to
              + "\r\n"
              + "Subject: Cocina Cero - verifica tu cuenta (Deber 3)\r\n"
              + "MIME-Version: 1.0\r\n"
              + "Content-Type: text/plain; charset=UTF-8\r\n"
              + "Content-Transfer-Encoding: base64\r\n\r\n"
              + Base64.getMimeEncoder()
                  .encodeToString(
                      ("Tu codigo de verificacion es: "
                              + codigo
                              + "\n"
                              + "Caduca en 10 minutos. Luego un administrador debe aprobar tu"
                              + " cuenta.\n"
                              + "Si no solicitaste el registro, ignora este mensaje.")
                          .getBytes(StandardCharsets.UTF_8));
      String raw =
          Base64.getUrlEncoder()
              .withoutPadding()
              .encodeToString(body.getBytes(StandardCharsets.UTF_8));
      HttpResponse<String> sent =
          http.send(
              HttpRequest.newBuilder(
                      URI.create("https://gmail.googleapis.com/gmail/v1/users/me/messages/send"))
                  .timeout(Duration.ofSeconds(20))
                  .header("Authorization", "Bearer " + access)
                  .header("Content-Type", "application/json")
                  .POST(
                      HttpRequest.BodyPublishers.ofString(
                          mapper.writeValueAsString(Map.of("raw", raw))))
                  .build(),
              HttpResponse.BodyHandlers.ofString());
      if (sent.statusCode() != 200) throw new IllegalStateException();
    } catch (Exception e) {
      if (e instanceof InterruptedException) Thread.currentThread().interrupt();
      throw new ApiError(
          502,
          "No se pudo enviar el correo. Revisa la autorización local de Gmail y la conexión a"
              + " Internet.");
    }
  }
}
