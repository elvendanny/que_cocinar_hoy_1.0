package com.cocinacero.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import com.cocinacero.auth.repository.*;
import com.cocinacero.auth.service.CorreoService;
import com.fasterxml.jackson.databind.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "spring.config.import=")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SeguridadIntegrationTest {
  @Autowired MockMvc mvc;
  @Autowired ObjectMapper json;
  @Autowired Usuarios users;
  @Autowired Verificaciones codes;
  @MockitoBean CorreoService mail;

  JsonNode call(String method, String path, Object body, String token, int expected)
      throws Exception {
    var req =
        switch (method) {
          case "POST" -> post(path);
          case "PATCH" -> patch(path);
          case "DELETE" -> delete(path);
          default -> get(path);
        };
    if (body != null)
      req.contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(body));
    if (token != null) req.header("Authorization", "Bearer " + token);
    var r = mvc.perform(req).andReturn().getResponse();
    assertEquals(expected, r.getStatus(), r.getContentAsString());
    return r.getContentAsString().isBlank()
        ? json.nullNode()
        : json.readTree(r.getContentAsString());
  }

  Map<String, String> cred(String e) {
    return Map.of("email", e, "password", "PracticaCocina2026!");
  }

  String admin() throws Exception {
    return call(
            "POST",
            "/api/auth/login",
            Map.of("email", "admin@test.local", "password", "AdminPrueba2026!"),
            null,
            200)
        .path("token")
        .asText();
  }

  @Test
  void flujoCompletoAprobacionRevocacionYRoles() throws Exception {
    var code = new AtomicReference<String>();
    doAnswer(
            i -> {
              code.set(i.getArgument(1));
              return null;
            })
        .when(mail)
        .enviar(eq("nuevo@test.local"), anyString());
    call("GET", "/api/auth/me", null, null, 401);
    call("POST", "/api/auth/login", cred("nuevo@test.local"), null, 401);
    var reg =
        call(
            "POST",
            "/api/auth/registro",
            Map.of(
                "nombre",
                "Nuevo Usuario",
                "email",
                "nuevo@test.local",
                "password",
                "PracticaCocina2026!"),
            null,
            201);
    long id = reg.path("usuario").path("id").asLong();
    assertEquals("PENDIENTE", reg.path("usuario").path("estado").asText());
    assertFalse(users.findById(id).orElseThrow().passwordHash.equals("PracticaCocina2026!"));
    call(
        "POST",
        "/api/auth/registro",
        Map.of(
            "nombre",
            "Nuevo Usuario",
            "email",
            "nuevo@test.local",
            "password",
            "PracticaCocina2026!"),
        null,
        409);
    call("POST", "/api/auth/login", cred("nuevo@test.local"), null, 403);
    String a = admin();
    call(
        "PATCH",
        "/api/usuarios/" + id + "/estado",
        Map.of("estado", "APROBADA", "motivo", "Revisión"),
        a,
        409);
    String wrong = code.get().equals("000000") ? "111111" : "000000";
    call(
        "POST",
        "/api/auth/verificar",
        Map.of("email", "nuevo@test.local", "codigo", wrong),
        null,
        400);
    assertEquals(1, codes.findById(id).orElseThrow().intentos);
    call(
        "POST",
        "/api/auth/verificar",
        Map.of("email", "nuevo@test.local", "codigo", code.get()),
        null,
        200);
    call("POST", "/api/auth/login", cred("nuevo@test.local"), null, 403);
    call(
        "PATCH",
        "/api/usuarios/" + id + "/estado",
        Map.of("estado", "APROBADA", "motivo", "Correo y datos revisados"),
        a,
        200);
    String t =
        call("POST", "/api/auth/login", cred("nuevo@test.local"), null, 200).path("token").asText();
    call("GET", "/api/auth/me", null, t, 200);
    call("GET", "/api/usuarios", null, t, 403);
    call("GET", "/api/internal/verificar", null, t, 401);
    assertEquals(
        200,
        mvc.perform(
                get("/api/internal/verificar")
                    .header("Authorization", "Bearer " + t)
                    .header("X-Internal-Key", "internal-test-key-abcdefghijklmnopqrstuvwxyz"))
            .andReturn()
            .getResponse()
            .getStatus());
    call(
        "PATCH",
        "/api/usuarios/" + id + "/estado",
        Map.of("estado", "BLOQUEADA", "motivo", "Prueba"),
        a,
        200);
    call("GET", "/api/auth/me", null, t, 401);
    call("POST", "/api/auth/login", cred("nuevo@test.local"), null, 403);
    call(
        "PATCH",
        "/api/usuarios/" + id + "/estado",
        Map.of("estado", "RECHAZADA", "motivo", "Prueba rechazo"),
        a,
        200);
    call("POST", "/api/auth/login", cred("nuevo@test.local"), null, 403);
    call(
        "PATCH",
        "/api/usuarios/" + id + "/estado",
        Map.of("estado", "APROBADA", "motivo", "Revisado otra vez"),
        a,
        200);
    call("GET", "/api/auth/me", null, t, 401);
    String fresh =
        call("POST", "/api/auth/login", cred("nuevo@test.local"), null, 200).path("token").asText();
    call("POST", "/api/auth/logout", null, fresh, 200);
    call("GET", "/api/auth/me", null, fresh, 401);
    fresh =
        call("POST", "/api/auth/login", cred("nuevo@test.local"), null, 200).path("token").asText();
    call("PATCH", "/api/usuarios/" + id + "/rol", Map.of("rol", "ADMIN"), a, 200);
    call("GET", "/api/auth/me", null, fresh, 401);
    call("GET", "/api/usuarios/" + id + "/revisiones", null, a, 200);
    call("DELETE", "/api/usuarios/" + id, null, a, 204);
    call("GET", "/api/usuarios/" + id, null, a, 404);
  }

  @Test
  void validacionesCodigoYReenvio() throws Exception {
    String e = "codigo@test.local";
    var code = new AtomicReference<String>();
    doAnswer(
            i -> {
              code.set(i.getArgument(1));
              return null;
            })
        .when(mail)
        .enviar(eq(e), anyString());
    call(
        "POST",
        "/api/auth/registro",
        Map.of("nombre", "Test", "email", "no-es-correo", "password", "corta"),
        null,
        400);
    call(
        "POST",
        "/api/auth/registro",
        Map.of("nombre", "Test", "email", e, "password", "PracticaCocina2026!", "rol", "ADMIN"),
        null,
        400);
    var r =
        call(
            "POST",
            "/api/auth/registro",
            Map.of("nombre", "Test", "email", e, "password", "PracticaCocina2026!"),
            null,
            201);
    long id = r.path("usuario").path("id").asLong();
    call("POST", "/api/auth/reenviar", Map.of("email", e), null, 429);
    var c = codes.findById(id).orElseThrow();
    c.venceEn = Instant.now().minusSeconds(1);
    codes.save(c);
    call("POST", "/api/auth/verificar", Map.of("email", e, "codigo", code.get()), null, 400);
    c = codes.findById(id).orElseThrow();
    c.enviadoEn = Instant.now().minusSeconds(61);
    codes.save(c);
    call("POST", "/api/auth/reenviar", Map.of("email", e), null, 200);
    for (int n = 0; n < 5; n++)
      call(
          "POST",
          "/api/auth/verificar",
          Map.of("email", e, "codigo", code.get().equals("000000") ? "111111" : "000000"),
          null,
          400);
    call("POST", "/api/auth/verificar", Map.of("email", e, "codigo", code.get()), null, 400);
  }

  @Test
  void falloCorreoNoDejaRegistroParcial() throws Exception {
    doThrow(new com.cocinacero.comun.ApiError(502, "Correo no disponible"))
        .when(mail)
        .enviar(eq("fallo@test.local"), anyString());
    call(
        "POST",
        "/api/auth/registro",
        Map.of("nombre", "Test", "email", "fallo@test.local", "password", "PracticaCocina2026!"),
        null,
        502);
    assertTrue(users.findByEmail("fallo@test.local").isEmpty());
  }

  @Test
  void corsNavegadorYServicioInterno() throws Exception {
    var allowed = mvc.perform(options("/api/auth/login").header("Origin", "http://localhost:4200")
        .header("Access-Control-Request-Method", "POST").header("Access-Control-Request-Headers", "content-type,authorization"))
        .andReturn().getResponse();
    assertEquals(200, allowed.getStatus());
    assertEquals("http://localhost:4200", allowed.getHeader("Access-Control-Allow-Origin"));
    assertNull(allowed.getHeader("Access-Control-Allow-Credentials"));
    var denied = mvc.perform(options("/api/auth/login").header("Origin", "https://ajeno.example")
        .header("Access-Control-Request-Method", "POST")).andReturn().getResponse();
    assertEquals(403, denied.getStatus());
    var internal = mvc.perform(options("/api/internal/verificar").header("Origin", "http://localhost:4200")
        .header("Access-Control-Request-Method", "GET").header("Access-Control-Request-Headers", "x-internal-key"))
        .andReturn().getResponse();
    assertEquals(403, internal.getStatus());
    var missing = mvc.perform(get("/api/auth/me").header("Origin", "http://localhost:4200")).andReturn().getResponse();
    assertEquals(401, missing.getStatus());
    assertEquals("http://localhost:4200", missing.getHeader("Access-Control-Allow-Origin"));
  }

  @Test
  void filtrosAdministrativosSinHashes() throws Exception {
    var adminToken = admin();
    var filtered = call("GET", "/api/usuarios?estado=APROBADA&emailVerificado=true&texto=admin@test.local&limit=1", null, adminToken, 200);
    assertEquals(1, filtered.path("total").asInt());
    assertEquals("admin@test.local", filtered.path("datos").get(0).path("email").asText());
    assertFalse(filtered.toString().contains("passwordHash"));
    assertFalse(filtered.toString().contains("codigoHash"));
    call("GET", "/api/usuarios?estado=INEXISTENTE", null, adminToken, 400);
    call("GET", "/api/usuarios?page=0", null, adminToken, 400);
  }
}
