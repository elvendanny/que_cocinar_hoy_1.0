package com.cocinacero.auth.repository;

import com.cocinacero.auth.model.Verificacion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface Verificaciones extends JpaRepository<Verificacion, Long> {}
