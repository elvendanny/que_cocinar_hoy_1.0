package com.cocinacero.auth.config;

import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class ErroresDatos {
  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<?> conflict(Exception e) {
    return ResponseEntity.status(409)
        .body(Map.of("error", "Registro duplicado o relacionado con otros datos."));
  }
}
