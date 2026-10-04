package com.cocinacero.cocina.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "archivos")
public class Archivo {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "receta_id")
  public Receta receta;

  @Column(nullable = false, length = 255)
  public String nombreOriginal;

  @Column(nullable = false, unique = true, length = 100)
  public String nombreDisco;

  @Column(length = 50)
  public String tipo;

  public long bytes;
  public Instant creadoEn = Instant.now();
}
