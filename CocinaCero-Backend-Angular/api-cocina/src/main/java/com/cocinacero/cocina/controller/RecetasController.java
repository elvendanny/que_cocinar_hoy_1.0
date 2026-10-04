package com.cocinacero.cocina.controller;

import com.cocinacero.cocina.dto.Datos;
import com.cocinacero.cocina.service.*;
import com.cocinacero.comun.Actor;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class RecetasController {
  private final RecetasService s;
  private final ArchivosService f;

  public RecetasController(RecetasService s, ArchivosService f) {
    this.s = s;
    this.f = f;
  }

  @GetMapping("/recetas")
  public Object list(
      @RequestParam(required = false) String texto,
      @RequestParam(required = false) Long categoriaId,
      @RequestParam(required = false) String dificultad,
      @RequestParam(defaultValue = "false") boolean mias,
      @AuthenticationPrincipal Actor a) {
    return s.list(texto, categoriaId, dificultad, mias ? a.id() : null);
  }

  @GetMapping("/recetas/pagina")
  public Object pagina(@RequestParam(required = false) String texto,
      @RequestParam(required = false) Long categoriaId,
      @RequestParam(required = false) String dificultad,
      @RequestParam(defaultValue = "false") boolean mias,
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "12") int limit,
      @AuthenticationPrincipal Actor a) {
    return s.pagina(texto, categoriaId, dificultad, mias ? a.id() : null, page, limit);
  }

  @PatchMapping("/recetas/{id}/portada")
  public Object portada(@PathVariable long id, @AuthenticationPrincipal Actor a, @Valid @RequestBody Datos.Portada d) {
    return s.portada(a, id, d.archivoId());
  }

  @GetMapping("/archivos/{id}/imagen")
  public ResponseEntity<byte[]> imagen(@PathVariable long id) {
    var file = f.get(id);
    if (!java.util.Set.of("image/png", "image/jpeg").contains(file.tipo))
      throw new com.cocinacero.comun.ApiError(400, "Este archivo no es una imagen.");
    return ResponseEntity.ok().contentType(MediaType.parseMediaType(file.tipo))
        .header("Content-Disposition", "inline")
        .header("Cache-Control", "no-store").body(f.bytes(id));
  }

  @GetMapping("/recetas/{id}")
  public Object get(@PathVariable long id) {
    return s.get(id);
  }

  @PostMapping("/recetas")
  @ResponseStatus(HttpStatus.CREATED)
  public Object save(@AuthenticationPrincipal Actor a, @Valid @RequestBody Datos.Receta d) {
    return s.save(a, null, d);
  }

  @PutMapping("/recetas/{id}")
  public Object save(
      @AuthenticationPrincipal Actor a, @PathVariable long id, @Valid @RequestBody Datos.Receta d) {
    return s.save(a, id, d);
  }

  @DeleteMapping("/recetas/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void remove(@AuthenticationPrincipal Actor a, @PathVariable long id) {
    s.remove(a, id);
  }

  @GetMapping("/recomendaciones")
  public Object recommendations(@RequestParam String ingredientes) {
    return s.recomendaciones(ingredientes);
  }

  @PostMapping(value = "/recetas/{id}/archivos", consumes = "multipart/form-data")
  @ResponseStatus(HttpStatus.CREATED)
  public Object upload(
      @AuthenticationPrincipal Actor a,
      @PathVariable long id,
      @RequestParam("archivo") MultipartFile archivo) {
    return f.subir(a, id, archivo);
  }

  @GetMapping("/archivos/{id}")
  public ResponseEntity<byte[]> download(@PathVariable long id) {
    var file = f.get(id);
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(file.tipo))
        .header(
            "Content-Disposition",
            ContentDisposition.attachment()
                .filename(file.nombreOriginal, java.nio.charset.StandardCharsets.UTF_8)
                .build()
                .toString())
        .body(f.bytes(id));
  }

  @DeleteMapping("/archivos/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@AuthenticationPrincipal Actor a, @PathVariable long id) {
    f.remove(a, id);
  }
}
