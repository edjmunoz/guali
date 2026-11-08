package edu.umg.desarrolloweb.guali.web.servicio;

import edu.umg.desarrolloweb.guali.web.dto.EstadisticasDto;
import edu.umg.desarrolloweb.guali.web.modelo.EstadoHistorial;
import edu.umg.desarrolloweb.guali.web.modelo.Visita;
import edu.umg.desarrolloweb.guali.web.repositorio.RegistroHistorialRepository;
import edu.umg.desarrolloweb.guali.web.repositorio.VisitaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Registro de visitas y estadísticas de uso para el administrador. */
@Service
public class VisitaService {

    private final VisitaRepository visitas;
    private final RegistroHistorialRepository historial;
    private final Clock reloj;

    public VisitaService(VisitaRepository visitas, RegistroHistorialRepository historial, Clock reloj) {
        this.visitas = visitas;
        this.historial = historial;
        this.reloj = reloj;
    }

    @Transactional
    public void registrar(String clienteId, String agenteUsuario) {
        visitas.save(new Visita(clienteId, LocalDateTime.now(reloj), agenteUsuario));
    }

    public LocalDate hoy() {
        return LocalDate.now(reloj);
    }

    @Transactional(readOnly = true)
    public EstadisticasDto estadisticas(LocalDate desde, LocalDate hasta) {
        if (hasta.isBefore(desde)) {
            LocalDate t = desde;
            desde = hasta;
            hasta = t;
        }
        if (desde.plusDays(366).isBefore(hasta)) {
            desde = hasta.minusDays(366);   // máximo un año para no saturar la tabla
        }

        Map<LocalDate, long[]> porDia = new HashMap<>();
        for (Object[] fila : visitas.visitasPorDia(desde, hasta)) {
            porDia.put((LocalDate) fila[0], new long[]{((Number) fila[1]).longValue(), ((Number) fila[2]).longValue()});
        }
        long maximo = porDia.values().stream().mapToLong(v -> v[0]).max().orElse(0);

        // Incluir también los días sin visitas para que la gráfica sea continua
        List<EstadisticasDto.Dia> dias = new ArrayList<>();
        for (LocalDate d = desde; !d.isAfter(hasta); d = d.plusDays(1)) {
            long[] v = porDia.getOrDefault(d, new long[]{0, 0});
            int porcentaje = maximo == 0 ? 0 : (int) Math.round(v[0] * 100.0 / maximo);
            dias.add(new EstadisticasDto.Dia(d, v[0], v[1], porcentaje));
        }

        LocalDateTime inicio = desde.atStartOfDay();
        LocalDateTime fin = hasta.plusDays(1).atStartOfDay().minusNanos(1);
        return new EstadisticasDto(desde, hasta,
                visitas.contarVisitas(desde, hasta),
                visitas.contarVisitantesUnicos(desde, hasta),
                historial.countByFechaInicioBetween(inicio, fin),
                historial.countByEstadoAndFechaFinBetween(EstadoHistorial.COMPLETADA, inicio, fin),
                dias, maximo);
    }
}
