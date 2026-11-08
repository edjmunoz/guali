package edu.umg.desarrolloweb.guali.web.repositorio;

import edu.umg.desarrolloweb.guali.web.modelo.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByUsername(String username);

    boolean existsByUsername(String username);

    long countByActivoTrue();

    List<Usuario> findAllByOrderByUsernameAsc();
}
