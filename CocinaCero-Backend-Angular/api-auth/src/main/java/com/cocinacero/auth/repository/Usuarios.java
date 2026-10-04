package com.cocinacero.auth.repository;

import com.cocinacero.auth.model.Usuario;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;

public interface Usuarios extends JpaRepository<Usuario, Long>, JpaSpecificationExecutor<Usuario> {
  Optional<Usuario> findByEmail(String email);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select u from Usuario u where u.email=:email")
  Optional<Usuario> locked(String email);
}
