package com.cocinacero.comun;

public record Actor(
    long id, String email, String nombre, String rol, String estado, boolean emailVerificado) {
  public boolean admin() {
    return "ADMIN".equals(rol);
  }

  public void requireAdmin() {
    if (!admin()) throw new ApiError(403, "Se requiere rol ADMIN.");
  }
}
