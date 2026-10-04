package com.cocinacero.cocina.repository;

import com.cocinacero.cocina.model.RecetaIngrediente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface Detalles extends JpaRepository<RecetaIngrediente, Long> {
  java.util.List<RecetaIngrediente> findByRecetaId(long id);

  boolean existsByIngredienteId(long id);

  void deleteByRecetaId(long id);
}
