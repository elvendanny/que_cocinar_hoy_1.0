package com.cocinacero.auth.controller;

import com.cocinacero.auth.dto.Datos;
import com.cocinacero.auth.service.*;
import com.cocinacero.comun.Actor;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class AuthController {
  private final AuthService service;

  public AuthController(AuthService s) {
    service = s;
  }

  @PostMapping("/auth/registro")
  @ResponseStatus(org.springframework.http.HttpStatus.CREATED)
  public Object registro(@Valid @RequestBody Datos.Registro d) {
    return service.registro(d);
  }

  @PostMapping("/auth/login")
  public Object login(@Valid @RequestBody Datos.Login d) {
    return service.login(d);
  }

  @PostMapping("/auth/verificar")
  public Object verificar(@Valid @RequestBody Datos.Codigo d) {
    return service.verificar(d);
  }

  @PostMapping("/auth/reenviar")
  public Object reenviar(@Valid @RequestBody Datos.Correo d) {
    return service.reenviar(d);
  }

  @GetMapping({"/auth/me", "/internal/verificar"})
  public Object me(@AuthenticationPrincipal Actor a) {
    return a;
  }

  @PatchMapping("/auth/me")
  public Object perfil(@AuthenticationPrincipal Actor a, @Valid @RequestBody Datos.Perfil d) {
    return service.perfil(a, d);
  }

  @PostMapping("/auth/logout")
  public Object logout(@AuthenticationPrincipal Actor a) {
    service.logout(a);
    return Map.of("mensaje", "Sesiones del usuario cerradas.");
  }
}
