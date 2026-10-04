package com.cocinacero.cocina.repository;

import com.cocinacero.cocina.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface Categorias extends JpaRepository<Categoria, Long> {
  boolean existsByNombreIgnoreCase(String nombre);
}
