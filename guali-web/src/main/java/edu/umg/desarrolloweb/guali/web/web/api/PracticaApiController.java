package edu.umg.desarrolloweb.guali.web.web.api;

import edu.umg.desarrolloweb.guali.web.config.ClienteIdFiltro;
import edu.umg.desarrolloweb.guali.web.dto.*;
import edu.umg.desarrolloweb.guali.web.modelo.Leccion;
import edu.umg.desarrolloweb.guali.web.modelo.RegistroHistorial;
import edu.umg.desarrolloweb.guali.web.servicio.HistorialService;
import edu.umg.desarrolloweb.guali.web.servicio.LeccionService;
import edu.umg.desarrolloweb.guali.web.servicio.PracticaService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API usada por la pantalla principal del estudiante:
 *   POST /api/sesiones               abrir lección (crea registro de historial)
 *   POST /api/sesiones/{id}/cerrar   cerrar lección
 *   POST /api/codigo/compilar        compilar código
 *   POST /api/codigo/ejecutar        ejecutar código
 *   GET  /api/historial              historial del navegador actual
 */
@RestController
@RequestMapping("/api")
public class PracticaApiController {

    private final LeccionService lecciones;
    private final HistorialService historial;
    private final PracticaService practica;

    public PracticaApiController(LeccionService lecciones, HistorialService historial, PracticaService practica) {
        this.lecciones = lecciones;
        this.historial = historial;
        this.practica = practica;
    }

    @PostMapping("/sesiones")
    public SesionDto abrir(@RequestAttribute(ClienteIdFiltro.ATRIBUTO) String clienteId,
                           @Valid @RequestBody AbrirLeccionRequest solicitud) {
        Leccion leccion = lecciones.buscar(solicitud.leccionId());
        RegistroHistorial registro = historial.abrir(clienteId, leccion);
        return new SesionDto(registro.getId(), lecciones.obtener(leccion.getId()));
    }

    @PostMapping("/sesiones/{id}/cerrar")
    public void cerrar(@RequestAttribute(ClienteIdFiltro.ATRIBUTO) String clienteId, @PathVariable Long id) {
        historial.cerrar(clienteId, id);
    }

    @PostMapping("/codigo/compilar")
    public ResultadoCompilarDto compilar(@RequestAttribute(ClienteIdFiltro.ATRIBUTO) String clienteId,
                                         @Valid @RequestBody CodigoRequest solicitud) {
        return practica.compilar(clienteId, solicitud.sesionId(), solicitud.codigo());
    }

    @PostMapping("/codigo/ejecutar")
    public ResultadoEjecutarDto ejecutar(@RequestAttribute(ClienteIdFiltro.ATRIBUTO) String clienteId,
                                         @Valid @RequestBody CodigoRequest solicitud) {
        return practica.ejecutar(clienteId, solicitud.sesionId(), solicitud.codigo());
    }

    @GetMapping("/historial")
    public List<HistorialDto> historial(@RequestAttribute(ClienteIdFiltro.ATRIBUTO) String clienteId) {
        return historial.listar(clienteId);
    }
}
