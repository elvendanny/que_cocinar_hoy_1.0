package com.cocinacero.auth.controller;

import com.cocinacero.auth.dto.Datos;
import com.cocinacero.auth.service.*;
import com.cocinacero.comun.Actor;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class UsuariosController {
  private final UsuariosService s;

  public UsuariosController(UsuariosService s) {
    this.s = s;
  }

  @GetMapping("/usuarios")
  public Object list(
      @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "50") int limit,
      @RequestParam(required = false) String texto,
      @RequestParam(required = false) String estado,
      @RequestParam(required = false) Boolean emailVerificado) {
    return s.list(page, limit, texto, estado, emailVerificado);
  }

  @GetMapping("/usuarios/{id}")
  public Object get(@PathVariable long id) {
    return AuthService.publico(s.get(id));
  }

  @PatchMapping("/usuarios/{id}/estado")
  public Object estado(
      @AuthenticationPrincipal Actor a,
      @PathVariable long id,
      @Valid @RequestBody Datos.Decision d) {
    return s.decision(a, id, d);
  }

  @PatchMapping("/usuarios/{id}/rol")
  public Object rol(
      @AuthenticationPrincipal Actor a, @PathVariable long id, @Valid @RequestBody Datos.Rol d) {
    return s.rol(a, id, d);
  }

  @DeleteMapping("/usuarios/{id}")
  @ResponseStatus(org.springframework.http.HttpStatus.NO_CONTENT)
  public void remove(@AuthenticationPrincipal Actor a, @PathVariable long id) {
    s.remove(a, id);
  }

  @GetMapping("/usuarios/{id}/revisiones")
  public Object revisiones(@PathVariable long id) {
    return s.revisiones(id);
  }

  @GetMapping("/roles")
  public Object roles() {
    return List.of("ADMIN", "USER");
  }
}
