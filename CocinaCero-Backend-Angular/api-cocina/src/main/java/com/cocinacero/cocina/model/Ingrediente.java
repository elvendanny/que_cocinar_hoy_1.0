package com.cocinacero.cocina.model;

import jakarta.persistence.*;

@Entity
@Table(name = "ingredientes")
public class Ingrediente {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @Column(nullable = false, unique = true, length = 80)
  public String nombre;

  @Column(length = 255)
  public String descripcion;

  @Column(nullable = false, length = 20)
  public String tipo;
}
