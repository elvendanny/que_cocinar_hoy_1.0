package com.cocinacero.cocina.repository;

import com.cocinacero.cocina.model.Archivo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface Archivos extends JpaRepository<Archivo, Long> {
  java.util.List<Archivo> findByRecetaId(long id);

  long countByRecetaId(long id);
}
