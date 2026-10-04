package com.cocinacero.comun;

import java.util.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class Errores {
  @ExceptionHandler(ApiError.class)
  public ResponseEntity<?> api(ApiError e) {
    return ResponseEntity.status(e.status).body(Map.of("error", e.getMessage()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<?> validation(MethodArgumentNotValidException e) {
    return ResponseEntity.badRequest()
        .body(
            Map.of(
                "error",
                "Datos inválidos",
                "detalles",
                e.getBindingResult().getFieldErrors().stream()
                    .map(
                        x ->
                            Map.of(
                                "campo",
                                x.getField(),
                                "mensaje",
                                Objects.toString(x.getDefaultMessage(), "Inválido")))
                    .toList()));
  }

  @ExceptionHandler({
    HttpMessageNotReadableException.class,
    MethodArgumentTypeMismatchException.class
  })
  public ResponseEntity<?> json(Exception e) {
    return ResponseEntity.badRequest()
        .body(Map.of("error", "JSON, identificador o valor inválido."));
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ResponseEntity<?> size(Exception e) {
    return ResponseEntity.status(413)
        .body(Map.of("error", "Archivo demasiado grande: máximo 5 MB."));
  }
}
