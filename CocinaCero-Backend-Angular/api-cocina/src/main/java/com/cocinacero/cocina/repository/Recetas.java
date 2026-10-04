package com.cocinacero.cocina.repository;

import com.cocinacero.cocina.model.Receta;
import org.springframework.data.jpa.repository.JpaRepository;

public interface Recetas extends JpaRepository<Receta, Long>, org.springframework.data.jpa.repository.JpaSpecificationExecutor<Receta> {
  boolean existsByCategoriaId(long id);
}
