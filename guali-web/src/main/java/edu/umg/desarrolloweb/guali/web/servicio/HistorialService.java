package edu.umg.desarrolloweb.guali.web.servicio;

import edu.umg.desarrolloweb.guali.web.dto.Fechas;
import edu.umg.desarrolloweb.guali.web.dto.HistorialDto;
import edu.umg.desarrolloweb.guali.web.modelo.EstadoHistorial;
import edu.umg.desarrolloweb.guali.web.modelo.Leccion;
import edu.umg.desarrolloweb.guali.web.modelo.RegistroHistorial;
import edu.umg.desarrolloweb.guali.web.repositorio.RegistroHistorialRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Historial de sesión (persistente en base de datos).
 * Cada vez que el estudiante abre una lección se crea un registro nuevo;
 * así quedan guardados todos los intentos y no solo el exitoso.
 */
@Service
public class HistorialService {

    private final RegistroHistorialRepository repositorio;
    private final Clock reloj;

    public HistorialService(RegistroHistorialRepository repositorio, Clock reloj) {
        this.repositorio = repositorio;
        this.reloj = reloj;
    }

    /** Abre un nuevo intento. Cierra (como Pendiente) cualquier lección que hubiera quedado abierta. */
    @Transactional
    public RegistroHistorial abrir(String clienteId, Leccion leccion) {
        LocalDateTime ahora = LocalDateTime.now(reloj);
        for (RegistroHistorial abierto : repositorio.findByClienteIdAndFechaFinIsNull(clienteId)) {
            abierto.setFechaFin(ahora);
        }
        RegistroHistorial r = new RegistroHistorial();
        r.setClienteId(clienteId);
        r.setLeccionId(leccion.getId());
        r.setLeccionTitulo(leccion.getTitulo());
        r.setObjetivo(leccion.getObjetivo());
        r.setFechaInicio(ahora);
        r.setEstado(EstadoHistorial.PENDIENTE);
        return repositorio.save(r);
    }

    /** Cerrar lección / abandonar: registra la fecha final si aún no tenía. */
    @Transactional
    public void cerrar(String clienteId, Long registroId) {
        RegistroHistorial r = buscarPropio(clienteId, registroId);
        if (r.estaAbierto()) {
            r.setFechaFin(LocalDateTime.now(reloj));
        }
    }

    /** Registra un intento de compilación y evalúa el objetivo. Devuelve true si se completó ahora. */
    @Transactional
    public boolean registrarCompilacion(String clienteId, Long registroId, boolean exitosa) {
        RegistroHistorial r = buscarPropio(clienteId, registroId);
        r.sumarIntentoCompilar();
        return completarSi(r, EvaluadorObjetivo.cumpleAlCompilar(r.getObjetivo(), exitosa));
    }

    @Transactional
    public boolean registrarEjecucion(String clienteId, Long registroId, boolean compilo, boolean ejecucionExitosa) {
        RegistroHistorial r = buscarPropio(clienteId, registroId);
        r.sumarIntentoEjecutar();
        return completarSi(r, EvaluadorObjetivo.cumpleAlEjecutar(r.getObjetivo(), compilo, ejecucionExitosa));
    }

    @Transactional(readOnly = true)
    public String estado(String clienteId, Long registroId) {
        return buscarPropio(clienteId, registroId).getEstado().getEtiqueta();
    }

    @Transactional(readOnly = true)
    public List<HistorialDto> listar(String clienteId) {
        return repositorio.findTop200ByClienteIdOrderByFechaInicioDesc(clienteId).stream()
                .map(r -> new HistorialDto(r.getId(), r.getLeccionTitulo(),
                        Fechas.formatear(r.getFechaInicio()),
                        r.getFechaFin() == null ? "En curso" : Fechas.formatear(r.getFechaFin()),
                        r.getEstado().getEtiqueta(), r.getIntentosCompilar(), r.getIntentosEjecutar()))
                .toList();
    }

    private boolean completarSi(RegistroHistorial r, boolean cumple) {
        // Solo cuenta si la lección sigue abierta y aún no estaba completada
        if (cumple && r.estaAbierto() && r.getEstado() == EstadoHistorial.PENDIENTE) {
            r.setEstado(EstadoHistorial.COMPLETADA);
            r.setFechaFin(LocalDateTime.now(reloj));
            return true;
        }
        return false;
    }

    private RegistroHistorial buscarPropio(String clienteId, Long registroId) {
        RegistroHistorial r = repositorio.findById(registroId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Sesión no encontrada"));
        if (!r.getClienteId().equals(clienteId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "La sesión no pertenece a este navegador");
        }
        return r;
    }
}
