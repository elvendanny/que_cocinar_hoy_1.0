package com.cocinacero.auth.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "revisiones")
public class Revision {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  public Long usuarioId;
  public Long administradorId;

  @Column(length = 15)
  public String estadoAnterior;

  @Column(length = 15)
  public String estadoNuevo;

  @Column(length = 500)
  public String motivo;

  public Instant fecha = Instant.now();
}
