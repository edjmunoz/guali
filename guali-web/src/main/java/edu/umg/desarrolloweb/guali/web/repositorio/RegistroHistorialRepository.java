package edu.umg.desarrolloweb.guali.web.repositorio;

import edu.umg.desarrolloweb.guali.web.modelo.EstadoHistorial;
import edu.umg.desarrolloweb.guali.web.modelo.RegistroHistorial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface RegistroHistorialRepository extends JpaRepository<RegistroHistorial, Long> {

    List<RegistroHistorial> findTop200ByClienteIdOrderByFechaInicioDesc(String clienteId);

    List<RegistroHistorial> findByClienteIdAndFechaFinIsNull(String clienteId);

    long countByFechaInicioBetween(LocalDateTime desde, LocalDateTime hasta);

    long countByEstadoAndFechaFinBetween(EstadoHistorial estado, LocalDateTime desde, LocalDateTime hasta);
}
