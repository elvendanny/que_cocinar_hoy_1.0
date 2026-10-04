package com.cocinacero.auth.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "verificaciones")
public class Verificacion {
  @Id public Long usuarioId;

  @Column(nullable = false, length = 100)
  public String codigoHash;

  public Instant venceEn;
  public Instant enviadoEn;
  public int intentos;
}
