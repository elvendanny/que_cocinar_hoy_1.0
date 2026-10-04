package com.cocinacero.cocina.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "recetas")
public class Receta {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @Column(nullable = false, length = 120)
  public String nombre;

  @Column(length = 500)
  public String descripcion;

  @Column(nullable = false, columnDefinition = "MEDIUMTEXT")
  public String instrucciones;

  public int tiempoPreparacion;
  public int porciones;

  @Column(length = 10)
  public String dificultad;

  @ManyToOne(optional = false)
  @JoinColumn(name = "categoria_id")
  public Categoria categoria;

  @Column(nullable = false)
  public Long usuarioId;

  public Instant creadoEn = Instant.now();

  /** Solo se asigna a un archivo de esta receta mediante el servicio. */
  public Long portadaArchivoId;

  /** Imagen incluida en el JAR para recetas de demostración, no editable desde el JSON. */
  @Column(length = 80)
  public String imagenDemo;
}
