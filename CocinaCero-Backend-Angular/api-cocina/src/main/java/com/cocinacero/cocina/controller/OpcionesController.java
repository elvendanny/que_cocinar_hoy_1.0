package com.cocinacero.cocina.controller;

import com.cocinacero.comun.ApiError;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class OpcionesController {
  @GetMapping("/opciones")
  public Object opciones() {
    return Map.of(
        "dificultades", List.of("FACIL", "MEDIA", "DIFICIL"),
        "tiposIngrediente", List.of("PROTEINA", "VEGETAL", "FRUTA", "LACTEO", "CEREAL", "CONDIMENTO", "OTRO"),
        "unidadesMedida", List.of("GRAMOS", "MILILITROS", "UNIDADES", "TAZAS", "CUCHARADAS", "CUCHARADITAS", "AL_GUSTO"),
        "archivos", Map.of("maxBytes", 5242880, "maxPorReceta", 10, "tipos", List.of("image/png", "image/jpeg", "application/pdf")));
  }

  @GetMapping("/imagenes-demo/{nombre}")
  public ResponseEntity<byte[]> imagen(@PathVariable String nombre) throws IOException {
    if (!List.of("receta.png", "ensalada.png", "tortilla.png", "pasta.png", "avena.png", "sopa.png", "tostadas.png").contains(nombre))
      throw new ApiError(404, "Imagen no encontrada.");
    var resource = new ClassPathResource("demo/imagenes/" + nombre);
    return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).header("Cache-Control", "no-store").body(resource.getContentAsByteArray());
  }
}
