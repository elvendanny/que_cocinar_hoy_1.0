package com.cocinacero.auth.service;

import com.cocinacero.auth.dto.Datos;
import com.cocinacero.auth.model.*;
import com.cocinacero.auth.repository.*;
import com.cocinacero.comun.*;
import java.time.Instant;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuariosService {
  private final Usuarios users;
  private final Verificaciones codes;
  private final Revisiones revisions;

  public UsuariosService(Usuarios u, Verificaciones c, Revisiones r) {
    users = u;
    codes = c;
    revisions = r;
  }

  public Usuario get(long id) {
    return users.findById(id).orElseThrow(() -> new ApiError(404, "Usuario no encontrado."));
  }

  public Object list(int page, int limit) { return list(page, limit, null, null, null); }

  @Transactional(readOnly = true)
  public Object list(int page, int limit, String texto, String estado, Boolean emailVerificado) {
    if (page < 1 || limit < 1 || limit > 100) throw new ApiError(400, "Paginación inválida.");
    if (estado != null && !Set.of("PENDIENTE", "APROBADA", "RECHAZADA", "BLOQUEADA").contains(estado))
      throw new ApiError(400, "Estado inválido.");
    org.springframework.data.jpa.domain.Specification<Usuario> filtro = (root, query, cb) -> {
      var condiciones = new ArrayList<jakarta.persistence.criteria.Predicate>();
      if (texto != null && !texto.isBlank()) {
        String valor = "%" + texto.trim().toLowerCase(Locale.ROOT).replace("!", "!!").replace("%", "!%").replace("_", "!_") + "%";
        condiciones.add(cb.or(cb.like(cb.lower(root.get("nombre")), valor, '!'), cb.like(cb.lower(root.get("email")), valor, '!')));
      }
      if (estado != null) condiciones.add(cb.equal(root.get("estado"), estado));
      if (emailVerificado != null) condiciones.add(cb.equal(root.get("emailVerificado"), emailVerificado));
      return cb.and(condiciones.toArray(jakarta.persistence.criteria.Predicate[]::new));
    };
    var p = users.findAll(filtro, PageRequest.of(page - 1, limit, Sort.by("id")));
    return Map.of(
        "datos",
        p.getContent().stream().map(AuthService::publico).toList(),
        "total",
        p.getTotalElements(),
        "page",
        page,
        "limit",
        limit);
  }

  @Transactional
  public Object decision(Actor a, long id, Datos.Decision d) {
    a.requireAdmin();
    if (id == a.id()) throw new ApiError(409, "No puedes cambiar tu propio estado.");
    var u = users.locked(get(id).email).orElseThrow();
    if (d.estado().equals("APROBADA") && !u.emailVerificado)
      throw new ApiError(409, "El usuario debe verificar su correo antes de ser aprobado.");
    var r = new Revision();
    r.usuarioId = id;
    r.administradorId = a.id();
    r.estadoAnterior = u.estado;
    r.estadoNuevo = d.estado();
    r.motivo = d.motivo().trim();
    revisions.save(r);
    u.estado = d.estado();
    u.revisadoPor = a.id();
    u.revisadoEn = Instant.now();
    u.motivoRevision = d.motivo().trim();
    u.tokenVersion++;
    return AuthService.publico(users.save(u));
  }

  @Transactional
  public Object rol(Actor a, long id, Datos.Rol d) {
    a.requireAdmin();
    if (id == a.id()) throw new ApiError(409, "No puedes cambiar tu propio rol.");
    var u = users.locked(get(id).email).orElseThrow();
    u.rol = d.rol();
    u.tokenVersion++;
    return AuthService.publico(users.save(u));
  }

  @Transactional
  public void remove(Actor a, long id) {
    a.requireAdmin();
    if (id == a.id()) throw new ApiError(409, "No puedes eliminar tu propia cuenta.");
    var u = get(id);
    codes.deleteById(id);
    users.delete(u);
  }

  public Object revisiones(long id) {
    get(id);
    return revisions.findAll().stream().filter(r -> r.usuarioId == id).toList();
  }
}
