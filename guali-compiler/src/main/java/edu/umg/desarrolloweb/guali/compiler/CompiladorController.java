package edu.umg.desarrolloweb.guali.compiler;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * API REST interna del microservicio.
 *   POST /api/compilar  { "codigo": "..." }
 *   POST /api/ejecutar  { "codigo": "..." }
 */
@RestController
@RequestMapping("/api")
public class CompiladorController {

    private final CompiladorService servicio;

    public CompiladorController(CompiladorService servicio) {
        this.servicio = servicio;
    }

    @PostMapping("/compilar")
    public RespuestaCompilacion compilar(@Valid @RequestBody SolicitudCodigo solicitud) {
        return servicio.compilar(solicitud.codigo());
    }

    @PostMapping("/ejecutar")
    public RespuestaEjecucion ejecutar(@Valid @RequestBody SolicitudCodigo solicitud) {
        return servicio.ejecutar(solicitud.codigo());
    }
}
