package edu.umg.desarrolloweb.guali.web.web.api;

import edu.umg.desarrolloweb.guali.web.dto.ResultadoImportacionDto;
import edu.umg.desarrolloweb.guali.web.servicio.LeccionService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/** Importar / exportar / eliminar lecciones. Solo administradores (/api/admin/**). */
@RestController
@RequestMapping("/api/admin/lecciones")
public class AdminLeccionApiController {

    private final LeccionService servicio;

    public AdminLeccionApiController(LeccionService servicio) {
        this.servicio = servicio;
    }

    @PostMapping(value = "/importar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResultadoImportacionDto importar(@RequestParam("archivo") MultipartFile archivo) throws IOException {
        if (archivo.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecciona un archivo .zip");
        }
        String nombre = archivo.getOriginalFilename() == null ? "" : archivo.getOriginalFilename().toLowerCase();
        if (!nombre.endsWith(".zip")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El archivo debe ser .zip");
        }
        return servicio.importar(archivo.getBytes());
    }

    @GetMapping("/exportar")
    public ResponseEntity<byte[]> exportar() {
        byte[] zip = servicio.exportar();
        String nombre = "lecciones-" + DateTimeFormatter.ofPattern("yyyyMMdd-HHmm").format(LocalDateTime.now()) + ".zip";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/zip"))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(nombre).build().toString())
                .body(zip);
    }

    @DeleteMapping("/{id}")
    public Map<String, String> eliminar(@PathVariable Long id) {
        String titulo = servicio.eliminar(id);
        return Map.of("mensaje", "Lección '" + titulo + "' eliminada.");
    }
}
