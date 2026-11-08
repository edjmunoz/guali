package edu.umg.desarrolloweb.guali.web.servicio;

import edu.umg.desarrolloweb.guali.web.dto.CompilacionRemota;
import edu.umg.desarrolloweb.guali.web.dto.EjecucionRemota;
import edu.umg.desarrolloweb.guali.web.dto.ResultadoCompilarDto;
import edu.umg.desarrolloweb.guali.web.dto.ResultadoEjecutarDto;
import org.springframework.stereotype.Service;

/** Une el compilador remoto con el historial (evaluación del objetivo de la lección). */
@Service
public class PracticaService {

    private final CompiladorCliente compilador;
    private final HistorialService historial;

    public PracticaService(CompiladorCliente compilador, HistorialService historial) {
        this.compilador = compilador;
        this.historial = historial;
    }

    public ResultadoCompilarDto compilar(String clienteId, Long sesionId, String codigo) {
        CompilacionRemota r = compilador.compilar(codigo);
        boolean cumplido = false;
        String estado = null;
        if (sesionId != null) {
            cumplido = historial.registrarCompilacion(clienteId, sesionId, r.exitoso());
            estado = historial.estado(clienteId, sesionId);
        }
        return new ResultadoCompilarDto(r.exitoso(), r.salida(), cumplido, estado);
    }

    public ResultadoEjecutarDto ejecutar(String clienteId, Long sesionId, String codigo) {
        EjecucionRemota r = compilador.ejecutar(codigo);
        boolean cumplido = false;
        String estado = null;
        if (sesionId != null) {
            cumplido = historial.registrarEjecucion(clienteId, sesionId, r.compilacionExitosa(), r.exitoso());
            estado = historial.estado(clienteId, sesionId);
        }
        return new ResultadoEjecutarDto(r.compilacionExitosa(), r.salidaCompilacion(), r.exitoso(), r.salida(),
                r.errores(), r.timeout(), cumplido, estado);
    }
}
