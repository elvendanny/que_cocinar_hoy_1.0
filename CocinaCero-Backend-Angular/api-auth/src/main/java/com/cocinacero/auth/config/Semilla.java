package com.cocinacero.auth.config;

import com.cocinacero.auth.model.Usuario;
import com.cocinacero.auth.repository.Usuarios;
import com.cocinacero.auth.service.AuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class Semilla implements CommandLineRunner {
  private final Usuarios users;
  private final String email, password;
  private final boolean enabled;

  public Semilla(
      Usuarios u,
      @Value("${app.admin-email}") String e,
      @Value("${app.admin-password}") String p,
      @Value("${app.seed.enabled}") boolean en) {
    users = u;
    email = e;
    password = p;
    enabled = en;
  }

  public void run(String... args) {
    if (!enabled || users.findByEmail(AuthService.email(email)).isPresent()) return;
    if (password.length() < 10
        || password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72)
      throw new IllegalStateException(
          "Configura la contraseña inicial del administrador (10 a 72 bytes).");
    var u = new Usuario();
    u.nombre = "Administrador Cocina Cero";
    u.email = AuthService.email(email);
    u.passwordHash = new BCryptPasswordEncoder(12).encode(password);
    u.rol = "ADMIN";
    u.estado = "APROBADA";
    u.emailVerificado = true;
    users.save(u);
  }
}
