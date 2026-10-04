package com.cocinacero.cocina;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import com.cocinacero.cocina.security.AuthCliente;
import com.cocinacero.comun.*;
import com.fasterxml.jackson.databind.*;
import java.util.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "spring.config.import=")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CocinaIntegrationTest {
  @Autowired MockMvc mvc;
  @Autowired ObjectMapper json;
  @MockitoBean AuthCliente auth;

  @BeforeEach
  void setup() {
    when(auth.verificar(any()))
        .thenAnswer(
            i -> {
              String h = i.getArgument(0);
              if (h == null) throw new ApiError(401, "Sin token");
              return switch (h) {
                case "Bearer admin" ->
                    new Actor(1, "a@test.local", "Admin", "ADMIN", "APROBADA", true);
                case "Bearer user" ->
                    new Actor(2, "u@test.local", "User", "USER", "APROBADA", true);
                case "Bearer otro" ->
                    new Actor(3, "o@test.local", "Otro", "USER", "APROBADA", true);
                default -> throw new ApiError(401, "Inválido");
              };
            });
  }

  JsonNode call(String method, String path, Object body, String token, int expected)
      throws Exception {
    var req =
        switch (method) {
          case "POST" -> post(path);
          case "PUT" -> put(path);
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

  @Test
  void crudPermisosRelacionesArchivos() throws Exception {
    call("GET", "/api/recetas", null, null, 401);
    call("POST", "/api/categorias", Map.of("nombre", "Prueba"), "user", 403);
    long c =
        call("POST", "/api/categorias", Map.of("nombre", "Prueba"), "admin", 201)
            .path("id")
            .asLong();
    call("POST", "/api/categorias", Map.of("nombre", "Prueba"), "admin", 409);
    call(
        "PUT",
        "/api/categorias/" + c,
        Map.of("nombre", "Prueba actualizada", "descripcion", "Editada"),
        "admin",
        200);
    long i =
        call(
                "POST",
                "/api/ingredientes",
                Map.of("nombre", "Ingrediente prueba", "tipo", "VEGETAL"),
                "admin",
                201)
            .path("id")
            .asLong();
    call(
        "PUT",
        "/api/ingredientes/" + i,
        Map.of("nombre", "Ingrediente editado", "tipo", "VEGETAL"),
        "admin",
        200);
    var recipe =
        new HashMap<String, Object>(
            Map.of(
                "nombre",
                "Receta prueba",
                "descripcion",
                "Descripción prueba",
                "instrucciones",
                "Cocinar y servir caliente.",
                "tiempoPreparacion",
                15,
                "porciones",
                2,
                "dificultad",
                "FACIL",
                "categoriaId",
                c,
                "ingredientes",
                List.of(
                    Map.of(
                        "ingredienteId",
                        i,
                        "cantidad",
                        1,
                        "unidadMedida",
                        "UNIDADES",
                        "opcional",
                        false))));
    long r = call("POST", "/api/recetas", recipe, "user", 201).path("id").asLong();
    call("GET", "/api/recetas/" + r, null, "user", 200);
    call("GET", "/api/recomendaciones?ingredientes=" + i, null, "user", 200);
    call("PUT", "/api/recetas/" + r, recipe, "otro", 403);
    recipe.put("porciones", 3);
    call("PUT", "/api/recetas/" + r, recipe, "user", 200);
    call("DELETE", "/api/categorias/" + c, null, "admin", 409);
    call("DELETE", "/api/ingredientes/" + i, null, "admin", 409);
    var bad = new MockMultipartFile("archivo", "mal.txt", "text/plain", "Hola".getBytes());
    assertEquals(
        400,
        mvc.perform(
                multipart("/api/recetas/" + r + "/archivos")
                    .file(bad)
                    .header("Authorization", "Bearer user"))
            .andReturn()
            .getResponse()
            .getStatus());
    var file =
        new MockMultipartFile(
            "archivo", "prueba.pdf", "application/pdf", "%PDF-1.4\n%%EOF".getBytes());
    assertEquals(
        403,
        mvc.perform(
                multipart("/api/recetas/" + r + "/archivos")
                    .file(file)
                    .header("Authorization", "Bearer otro"))
            .andReturn()
            .getResponse()
            .getStatus());
    var up =
        mvc.perform(
                multipart("/api/recetas/" + r + "/archivos")
                    .file(file)
                    .header("Authorization", "Bearer user"))
            .andReturn()
            .getResponse();
    assertEquals(201, up.getStatus(), up.getContentAsString());
    long f = json.readTree(up.getContentAsString()).path("id").asLong();
    assertEquals(
        200,
        mvc.perform(get("/api/archivos/" + f).header("Authorization", "Bearer user"))
            .andReturn()
            .getResponse()
            .getStatus());
    call("DELETE", "/api/recetas/" + r, null, "user", 409);
    call("DELETE", "/api/archivos/" + f, null, "otro", 403);
    call("DELETE", "/api/archivos/" + f, null, "user", 204);
    call("DELETE", "/api/recetas/" + r, null, "user", 204);
    call("GET", "/api/recetas/" + r, null, "user", 404);
    call("DELETE", "/api/ingredientes/" + i, null, "admin", 204);
    call("DELETE", "/api/categorias/" + c, null, "admin", 204);
  }

  @Test
  void validaciones() {
    assertDoesNotThrow(
        () -> {
          call("POST", "/api/recetas", Map.of("nombre", "Incompleta"), "user", 400);
          call(
              "POST",
              "/api/ingredientes",
              Map.of("nombre", "Tipo incorrecto", "tipo", "INVALIDO"),
              "admin",
              400);
          call("GET", "/api/recomendaciones?ingredientes=abc", null, "user", 400);
          call("GET", "/api/recetas/999999", null, "user", 404);
        });
  }

  @Test
  void corsAntesDelJwtYProteccionDeImagenes() throws Exception {
    var preflight = mvc.perform(options("/api/recetas/1/archivos").header("Origin", "http://localhost:4200")
        .header("Access-Control-Request-Method", "POST").header("Access-Control-Request-Headers", "authorization"))
        .andReturn().getResponse();
    assertEquals(200, preflight.getStatus());
    verify(auth, never()).verificar(any());
    assertEquals("http://localhost:4200", preflight.getHeader("Access-Control-Allow-Origin"));
    assertEquals(403, mvc.perform(options("/api/recetas").header("Origin", "https://ajeno.example")
        .header("Access-Control-Request-Method", "GET")).andReturn().getResponse().getStatus());
    assertEquals(401, mvc.perform(get("/api/imagenes-demo/ensalada.png")).andReturn().getResponse().getStatus());
    var image = mvc.perform(get("/api/imagenes-demo/ensalada.png").header("Authorization", "Bearer user"))
        .andReturn().getResponse();
    assertEquals(200, image.getStatus());
    assertEquals("image/png", image.getContentType());
    assertNotNull(javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(image.getContentAsByteArray())));
    call("GET", "/api/imagenes-demo/no-existe.png", null, "user", 404);
    call("GET", "/api/opciones", null, "user", 200);
  }

  @Test
  void galeriaPortadaPaginacionYPropiedad() throws Exception {
    long cat = call("GET", "/api/categorias", null, "user", 200).get(0).path("id").asLong();
    long ing = call("GET", "/api/ingredientes", null, "user", 200).get(0).path("id").asLong();
    var body = Map.of("nombre", "Receta galería", "descripcion", "Prueba de galería web",
        "instrucciones", "Preparar los ingredientes y servir.", "tiempoPreparacion", 10, "porciones", 2,
        "dificultad", "FACIL", "categoriaId", cat,
        "ingredientes", List.of(Map.of("ingredienteId", ing, "cantidad", 1, "unidadMedida", "UNIDADES", "opcional", false)));
    long receta = call("POST", "/api/recetas", body, "user", 201).path("id").asLong();
    long ajena = call("POST", "/api/recetas", body, "otro", 201).path("id").asLong();
    var mine = call("GET", "/api/recetas/pagina?mias=true&texto=galería&limit=1", null, "user", 200);
    assertEquals(1, mine.path("total").asInt());
    assertEquals(receta, mine.path("datos").get(0).path("id").asLong());
    assertEquals(0, call("GET", "/api/recetas/pagina?mias=true&texto=galería&page=2&limit=1", null, "user", 200).path("datos").size());
    call("GET", "/api/recetas/pagina?limit=51", null, "user", 400);
    call("GET", "/api/recetas?dificultad=ERROR", null, "user", 400);
    byte[] png = new org.springframework.core.io.ClassPathResource("demo/imagenes/tortilla.png").getContentAsByteArray();
    var uploaded = mvc.perform(multipart("/api/recetas/" + receta + "/archivos")
        .file(new MockMultipartFile("archivo", "foto.png", "image/png", png)).header("Authorization", "Bearer user"))
        .andReturn().getResponse();
    assertEquals(201, uploaded.getStatus(), uploaded.getContentAsString());
    long archivo = json.readTree(uploaded.getContentAsString()).path("id").asLong();
    call("PATCH", "/api/recetas/" + receta + "/portada", Map.of("archivoId", archivo), "otro", 403);
    call("PATCH", "/api/recetas/" + ajena + "/portada", Map.of("archivoId", archivo), "otro", 400);
    var cover = call("PATCH", "/api/recetas/" + receta + "/portada", Map.of("archivoId", archivo), "user", 200);
    assertEquals(archivo, cover.path("imagenPrincipal").path("archivoId").asLong());
    var download = mvc.perform(get("/api/archivos/" + archivo + "/imagen")
        .header("Origin", "http://localhost:4200").header("Authorization", "Bearer user")).andReturn().getResponse();
    assertEquals(200, download.getStatus());
    assertArrayEquals(png, download.getContentAsByteArray());
    assertEquals("inline", download.getHeader("Content-Disposition"));
    assertEquals("Content-Disposition", download.getHeader("Access-Control-Expose-Headers"));
    byte[] truncated = Arrays.copyOf(png, 12);
    assertEquals(400, mvc.perform(multipart("/api/recetas/" + receta + "/archivos")
        .file(new MockMultipartFile("archivo", "rota.png", "image/png", truncated)).header("Authorization", "Bearer user"))
        .andReturn().getResponse().getStatus());
    call("DELETE", "/api/archivos/" + archivo, null, "user", 204);
    assertEquals("DEMO", call("GET", "/api/recetas/" + receta, null, "user", 200).path("imagenPrincipal").path("origen").asText());
    call("DELETE", "/api/recetas/" + receta, null, "user", 204);
    call("DELETE", "/api/recetas/" + ajena, null, "otro", 204);
  }
}
