package com.cocinacero.auth.service;

import com.cocinacero.auth.dto.Datos;
import com.cocinacero.auth.model.*;
import com.cocinacero.auth.repository.*;
import com.cocinacero.comun.*;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
  private final Usuarios users;
  private final Verificaciones codes;
  private final CorreoService correo;
  private final JwtService jwt;
  private final BCryptPasswordEncoder hash = new BCryptPasswordEncoder(12);
  private final String dummy = new BCryptPasswordEncoder(12).encode("DummyCredential123!");

  public AuthService(Usuarios u, Verificaciones c, CorreoService m, JwtService j) {
    users = u;
    codes = c;
    correo = m;
    jwt = j;
  }

  public static String email(String e) {
    return e.trim().toLowerCase(Locale.ROOT);
  }

  public static Actor actor(Usuario u) {
    return new Actor(u.id, u.email, u.nombre, u.rol, u.estado, u.emailVerificado);
  }

  public static Map<String, Object> publico(Usuario u) {
    var m = new LinkedHashMap<String, Object>();
    m.put("id", u.id);
    m.put("nombre", u.nombre);
    m.put("email", u.email);
    m.put("rol", u.rol);
    m.put("estado", u.estado);
    m.put("emailVerificado", u.emailVerificado);
    m.put("creadoEn", u.creadoEn);
    m.put("revisadoEn", u.revisadoEn);
    m.put("revisadoPor", u.revisadoPor);
    m.put("motivoRevision", u.motivoRevision);
    return m;
  }

  private void enviar(Usuario u) {
    var old = codes.findById(u.id);
    if (old.isPresent() && old.get().enviadoEn.plusSeconds(60).isAfter(Instant.now()))
      throw new ApiError(429, "Espera 60 segundos antes de reenviar.");
    String code = String.format("%06d", new SecureRandom().nextInt(1000000));
    var c = new Verificacion();
    c.usuarioId = u.id;
    c.codigoHash = hash.encode(code);
    c.enviadoEn = Instant.now();
    c.venceEn = c.enviadoEn.plusSeconds(600);
    codes.save(c);
    correo.enviar(u.email, code);
  }

  @Transactional
  public Object registro(Datos.Registro d) {
    if (d.password().getBytes(StandardCharsets.UTF_8).length > 72)
      throw new ApiError(400, "Contraseña: máximo 72 bytes UTF-8.");
    if (users.findByEmail(email(d.email())).isPresent())
      throw new ApiError(409, "El correo ya está registrado.");
    var u = new Usuario();
    u.nombre = d.nombre().trim();
    u.email = email(d.email());
    u.passwordHash = hash.encode(d.password());
    users.saveAndFlush(u);
    enviar(u);
    return Map.of(
        "usuario",
        publico(u),
        "mensaje",
        "Revisa tu correo. Tras verificarlo, un administrador debe aprobar la cuenta.");
  }

  @Transactional
  public Object reenviar(Datos.Correo d) {
    var u =
        users
            .locked(email(d.email()))
            .orElseThrow(() -> new ApiError(404, "Usuario no encontrado."));
    if (u.emailVerificado) throw new ApiError(409, "El correo ya está verificado.");
    if (!u.estado.equals("PENDIENTE"))
      throw new ApiError(403, "Cuenta no habilitada para verificar.");
    enviar(u);
    return Map.of("mensaje", "Código enviado.");
  }

  @Transactional(noRollbackFor = ApiError.class)
  public Object verificar(Datos.Codigo d) {
    var u = users.locked(email(d.email())).orElseThrow(() -> new ApiError(400, "Código inválido."));
    if (u.emailVerificado) throw new ApiError(409, "El correo ya está verificado.");
    if (!u.estado.equals("PENDIENTE"))
      throw new ApiError(403, "Cuenta no habilitada para verificar.");
    var c = codes.findById(u.id).orElseThrow(() -> new ApiError(400, "Solicita un código nuevo."));
    if (c.venceEn.isBefore(Instant.now()) || c.intentos >= 5)
      throw new ApiError(400, "Código vencido o intentos agotados; solicita otro.");
    c.intentos++;
    codes.save(c);
    if (!hash.matches(d.codigo(), c.codigoHash)) throw new ApiError(400, "Código incorrecto.");
    u.emailVerificado = true;
    users.save(u);
    codes.delete(c);
    return Map.of(
        "usuario",
        publico(u),
        "mensaje",
        "Correo verificado. La cuenta permanece PENDIENTE de aprobación del administrador.");
  }

  public Object login(Datos.Login d) {
    var found = users.findByEmail(email(d.email()));
    if (d.password().getBytes(StandardCharsets.UTF_8).length > 72)
      throw new ApiError(401, "Credenciales incorrectas.");
    boolean ok = hash.matches(d.password(), found.map(u -> u.passwordHash).orElse(dummy));
    if (found.isEmpty() || !ok) throw new ApiError(401, "Credenciales incorrectas.");
    var u = found.get();
    if (!u.emailVerificado) throw new ApiError(403, "Primero verifica tu correo.");
    if (!u.estado.equals("APROBADA"))
      throw new ApiError(403, "Cuenta " + u.estado + ". Se requiere aprobación del administrador.");
    return Map.of(
        "token", jwt.emitir(u), "tipo", "Bearer", "expiraEn", 3600, "usuario", publico(u));
  }

  public Actor autenticar(String token) {
    var j = jwt.leer(token);
    try {
      var u = users.findById(Long.parseLong(j.getSubject())).orElseThrow();
      Number v = j.getClaim("ver");
      if (v == null
          || v.longValue() != u.tokenVersion
          || !u.emailVerificado
          || !u.estado.equals("APROBADA")) throw new IllegalArgumentException();
      return actor(u);
    } catch (Exception e) {
      throw new ApiError(401, "Sesión inválida, revocada o cuenta no aprobada.");
    }
  }

  @Transactional
  public Object perfil(Actor actor, Datos.Perfil d) {
    var u = users.findById(actor.id()).orElseThrow();
    u.nombre = d.nombre().trim();
    return publico(users.save(u));
  }

  @Transactional
  public void logout(Actor actor) {
    var u = users.locked(actor.email()).orElseThrow();
    u.tokenVersion++;
    users.save(u);
  }
}
