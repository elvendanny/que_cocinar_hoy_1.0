package com.cocinacero.auth.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "usuarios", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class Usuario {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @Column(nullable = false, length = 80)
  public String nombre;

  @Column(nullable = false, length = 254)
  public String email;

  @Column(nullable = false, length = 100)
  public String passwordHash;

  @Column(nullable = false, length = 10)
  public String rol = "USER";

  @Column(nullable = false, length = 15)
  public String estado = "PENDIENTE";

  public boolean emailVerificado = false;
  public long tokenVersion = 0;
  public Instant creadoEn = Instant.now();
  public Instant revisadoEn;
  public Long revisadoPor;

  @Column(length = 500)
  public String motivoRevision;
}
