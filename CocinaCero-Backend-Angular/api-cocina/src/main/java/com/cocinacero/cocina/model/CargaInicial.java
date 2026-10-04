package com.cocinacero.cocina.model;
import jakarta.persistence.*;
import java.time.Instant;
@Entity
@Table(name = "cargas_iniciales")
public class CargaInicial {
  @Id @Column(length = 80) public String clave;
  public Instant fecha = Instant.now();
}
