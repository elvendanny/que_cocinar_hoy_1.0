package com.cocinacero.cocina.controller;

import java.util.Map;
import org.springframework.web.bind.annotation.*;

@RestController
public class HealthController {
  @GetMapping("/health")
  public Object health() {
    return Map.of("servicio", "api-cocina", "estado", "OK");
  }
}
