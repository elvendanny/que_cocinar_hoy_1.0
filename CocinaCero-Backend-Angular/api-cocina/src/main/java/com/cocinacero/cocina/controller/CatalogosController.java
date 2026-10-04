package com.cocinacero.cocina.controller;

import com.cocinacero.cocina.dto.Datos;
import com.cocinacero.cocina.service.CatalogosService;
import com.cocinacero.comun.Actor;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class CatalogosController {
  private final CatalogosService s;

  public CatalogosController(CatalogosService s) {
    this.s = s;
  }

  @GetMapping("/categorias")
  public Object cats() {
    return s.categorias();
  }

  @GetMapping("/categorias/{id}")
  public Object cat(@PathVariable long id) {
    return s.categoria(id);
  }

  @PostMapping("/categorias")
  @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
  public Object cat(@AuthenticationPrincipal Actor a, @Valid @RequestBody Datos.Categoria d) {
    return s.categoria(a, null, d);
  }

  @PutMapping("/categorias/{id}")
  public Object cat(
      @AuthenticationPrincipal Actor a,
      @PathVariable long id,
      @Valid @RequestBody Datos.Categoria d) {
    return s.categoria(a, id, d);
  }

  @DeleteMapping("/categorias/{id}")
  @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
  public void cat(@AuthenticationPrincipal Actor a, @PathVariable long id) {
    s.borrarCategoria(a, id);
  }

  @GetMapping("/ingredientes")
  public Object ings(
      @RequestParam(required = false) String texto, @RequestParam(required = false) String tipo) {
    return s.ingredientes(texto, tipo);
  }

  @GetMapping("/ingredientes/{id}")
  public Object ing(@PathVariable long id) {
    return s.ingrediente(id);
  }

  @PostMapping("/ingredientes")
  @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
  public Object ing(@AuthenticationPrincipal Actor a, @Valid @RequestBody Datos.Ingrediente d) {
    return s.ingrediente(a, null, d);
  }

  @PutMapping("/ingredientes/{id}")
  public Object ing(
      @AuthenticationPrincipal Actor a,
      @PathVariable long id,
      @Valid @RequestBody Datos.Ingrediente d) {
    return s.ingrediente(a, id, d);
  }

  @DeleteMapping("/ingredientes/{id}")
  @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
  public void ing(@AuthenticationPrincipal Actor a, @PathVariable long id) {
    s.borrarIngrediente(a, id);
  }
}
