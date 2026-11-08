package edu.umg.desarrolloweb.guali.web.web.api;

import edu.umg.desarrolloweb.guali.web.config.ClienteIdFiltro;
import edu.umg.desarrolloweb.guali.web.dto.PreferenciaDto;
import edu.umg.desarrolloweb.guali.web.servicio.PreferenciaService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/** Configuración (tamaño de fuente y tema) guardada en la base de datos por navegador. */
@RestController
@RequestMapping("/api/preferencias")
public class PreferenciaApiController {

    private final PreferenciaService servicio;

    public PreferenciaApiController(PreferenciaService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public PreferenciaDto obtener(@RequestAttribute(ClienteIdFiltro.ATRIBUTO) String clienteId) {
        return servicio.obtener(clienteId);
    }

    @PutMapping
    public PreferenciaDto guardar(@RequestAttribute(ClienteIdFiltro.ATRIBUTO) String clienteId,
                                  @Valid @RequestBody PreferenciaDto dto) {
        return servicio.guardar(clienteId, dto);
    }
}
