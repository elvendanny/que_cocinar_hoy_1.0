package com.cocinacero.cocina.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(
    name = "receta_ingredientes",
    uniqueConstraints = @UniqueConstraint(columnNames = {"receta_id", "ingrediente_id"}))
public class RecetaIngrediente {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "receta_id")
  public Receta receta;

  @ManyToOne(optional = false)
  @JoinColumn(name = "ingrediente_id")
  public Ingrediente ingrediente;

  @Column(precision = 10, scale = 2)
  public BigDecimal cantidad;

  @Column(length = 20)
  public String unidadMedida;

  public boolean opcional;
}
