package com.cocinacero.cocina.repository;

import com.cocinacero.cocina.model.Ingrediente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface Ingredientes extends JpaRepository<Ingrediente, Long> {
  boolean existsByNombreIgnoreCase(String nombre);
}
