package com.cocinacero.cocina.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public class Datos {
  public record Portada(@Positive Long archivoId) {}

  public record Categoria(
      @NotBlank @Size(min = 2, max = 80) String nombre, @Size(max = 255) String descripcion) {}

  public record Ingrediente(
      @NotBlank @Size(min = 2, max = 80) String nombre,
      @Size(max = 255) String descripcion,
      @NotBlank @Pattern(regexp = "PROTEINA|VEGETAL|FRUTA|LACTEO|CEREAL|CONDIMENTO|OTRO")
          String tipo) {}

  public record Detalle(
      @NotNull @Positive Long ingredienteId,
      @NotNull @DecimalMin("0.01") @DecimalMax("99999999.99") @Digits(integer = 8, fraction = 2)
          BigDecimal cantidad,
      @NotBlank
          @Pattern(regexp = "GRAMOS|MILILITROS|UNIDADES|TAZAS|CUCHARADAS|CUCHARADITAS|AL_GUSTO")
          String unidadMedida,
      boolean opcional) {}

  public record Receta(
      @NotBlank @Size(min = 2, max = 120) String nombre,
      @NotBlank @Size(min = 5, max = 500) String descripcion,
      @NotBlank @Size(min = 10, max = 20000) String instrucciones,
      @Min(1) @Max(1440) int tiempoPreparacion,
      @Min(1) @Max(100) int porciones,
      @NotBlank @Pattern(regexp = "FACIL|MEDIA|DIFICIL") String dificultad,
      @NotNull @Positive Long categoriaId,
      @NotEmpty @Size(max = 50) List<@Valid Detalle> ingredientes) {}
}
