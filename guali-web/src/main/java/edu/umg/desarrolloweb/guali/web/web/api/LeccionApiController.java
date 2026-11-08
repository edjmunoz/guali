package edu.umg.desarrolloweb.guali.web.web.api;

import edu.umg.desarrolloweb.guali.web.dto.LeccionDetalleDto;
import edu.umg.desarrolloweb.guali.web.dto.LeccionResumenDto;
import edu.umg.desarrolloweb.guali.web.modelo.RecursoLeccion;
import edu.umg.desarrolloweb.guali.web.servicio.LeccionService;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;

/** API pública de lecciones (lectura). */
@RestController
@RequestMapping("/api/lecciones")
public class LeccionApiController {

    private final LeccionService servicio;

    public LeccionApiController(LeccionService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<LeccionResumenDto> listar() {
        return servicio.listar();
    }

    @GetMapping("/{id}")
    public LeccionDetalleDto obtener(@PathVariable Long id) {
        return servicio.obtener(id);
    }

    /** Imágenes de la lección, ej. /api/lecciones/3/recursos/imagen.jpg */
    @GetMapping("/{id}/recursos/{*ruta}")
    public ResponseEntity<byte[]> recurso(@PathVariable Long id, @PathVariable String ruta) {
        String nombre = ruta.startsWith("/") ? ruta.substring(1) : ruta;
        return servicio.obtenerRecurso(id, nombre)
                .map(this::respuesta)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private ResponseEntity<byte[]> respuesta(RecursoLeccion r) {
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(r.getTipoContenido()))
                .cacheControl(CacheControl.maxAge(Duration.ofHours(1)))
                // Si alguien abre un SVG directamente, no se ejecuta ningún script
                .header("Content-Security-Policy", "default-src 'none'; style-src 'unsafe-inline'; sandbox")
                .body(r.getDatos());
    }
}
