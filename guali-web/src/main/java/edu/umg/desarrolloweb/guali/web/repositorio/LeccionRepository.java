package edu.umg.desarrolloweb.guali.web.repositorio;

import edu.umg.desarrolloweb.guali.web.modelo.Leccion;
import edu.umg.desarrolloweb.guali.web.modelo.Objetivo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LeccionRepository extends JpaRepository<Leccion, Long> {

    /** Proyección liviana para listar lecciones sin cargar contenido ni imágenes. */
    interface LeccionResumen {
        Long getId();
        String getTitulo();
        Objetivo getObjetivo();
    }

    List<LeccionResumen> findAllByOrderByTituloAsc();

    Optional<Leccion> findByTitulo(String titulo);

    List<Leccion> findAllByOrderByTitulo();
}
