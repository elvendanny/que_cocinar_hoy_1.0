package com.cocinacero.auth.dto;

import jakarta.validation.constraints.*;

public class Datos {
  public record Registro(
      @NotBlank @Size(min = 2, max = 80) String nombre,
      @NotBlank @Email @Size(max = 254) String email,
      @NotBlank
          @Size(min = 10, max = 72)
          @Pattern(
              regexp = "(?s)(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9]).*",
              message = "Incluye mayúscula, minúscula y número")
          String password) {}

  public record Login(@NotBlank @Email String email, @NotBlank @Size(max = 72) String password) {}

  public record Correo(@NotBlank @Email String email) {}

  public record Codigo(
      @NotBlank @Email String email, @NotBlank @Pattern(regexp = "[0-9]{6}") String codigo) {}

  public record Perfil(@NotBlank @Size(min = 2, max = 80) String nombre) {}

  public record Decision(
      @NotBlank @Pattern(regexp = "APROBADA|RECHAZADA|BLOQUEADA|PENDIENTE") String estado,
      @NotBlank @Size(max = 500) String motivo) {}

  public record Rol(@NotBlank @Pattern(regexp = "USER|ADMIN") String rol) {}
}
