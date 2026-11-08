package edu.umg.desarrolloweb.guali.web.repositorio;

import edu.umg.desarrolloweb.guali.web.modelo.RecursoLeccion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecursoLeccionRepository extends JpaRepository<RecursoLeccion, Long> {

    List<RecursoLeccion> findByLeccion_IdAndNombre(Long leccionId, String nombre);
}
