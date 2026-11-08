package edu.umg.desarrolloweb.guali.web.repositorio;

import edu.umg.desarrolloweb.guali.web.modelo.Visita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface VisitaRepository extends JpaRepository<Visita, Long> {

    @Query("select count(v) from Visita v where v.dia between :desde and :hasta")
    long contarVisitas(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);

    @Query("select count(distinct v.clienteId) from Visita v where v.dia between :desde and :hasta")
    long contarVisitantesUnicos(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);

    /** Filas: [dia (LocalDate), visitas (Long), visitantesUnicos (Long)] */
    @Query("select v.dia, count(v), count(distinct v.clienteId) from Visita v "
            + "where v.dia between :desde and :hasta group by v.dia order by v.dia")
    List<Object[]> visitasPorDia(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);
}
