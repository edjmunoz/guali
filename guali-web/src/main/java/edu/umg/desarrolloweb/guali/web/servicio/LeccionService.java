package edu.umg.desarrolloweb.guali.web.servicio;

import edu.umg.desarrolloweb.guali.web.dto.LeccionDetalleDto;
import edu.umg.desarrolloweb.guali.web.dto.LeccionResumenDto;
import edu.umg.desarrolloweb.guali.web.dto.ResultadoImportacionDto;
import edu.umg.desarrolloweb.guali.web.modelo.Leccion;
import edu.umg.desarrolloweb.guali.web.modelo.RecursoLeccion;
import edu.umg.desarrolloweb.guali.web.repositorio.LeccionRepository;
import edu.umg.desarrolloweb.guali.web.repositorio.RecursoLeccionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class LeccionService {

    private final LeccionRepository lecciones;
    private final RecursoLeccionRepository recursos;
    private final Clock reloj;

    public LeccionService(LeccionRepository lecciones, RecursoLeccionRepository recursos, Clock reloj) {
        this.lecciones = lecciones;
        this.recursos = recursos;
        this.reloj = reloj;
    }

    @Transactional(readOnly = true)
    public List<LeccionResumenDto> listar() {
        return lecciones.findAllByOrderByTituloAsc().stream()
                .map(l -> new LeccionResumenDto(l.getId(), l.getTitulo(), l.getObjetivo().name(),
                        l.getObjetivo().getDescripcion()))
                .toList();
    }

    @Transactional(readOnly = true)
    public LeccionDetalleDto obtener(Long id) {
        Leccion l = buscar(id);
        return new LeccionDetalleDto(l.getId(), l.getTitulo(), l.getObjetivo().name(),
                l.getObjetivo().getDescripcion(), l.getContenidoMd());
    }

    @Transactional(readOnly = true)
    public Leccion buscar(Long id) {
        return lecciones.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "La lección no existe"));
    }

    @Transactional(readOnly = true)
    public Optional<RecursoLeccion> obtenerRecurso(Long leccionId, String nombre) {
        return recursos.findByLeccion_IdAndNombre(leccionId, nombre).stream().findFirst();
    }

    @Transactional(readOnly = true)
    public long contar() {
        return lecciones.count();
    }

    /**
     * Importa un bundle .zip. Si ya existe una lección con el mismo título
     * (nombre de carpeta) se reemplaza su contenido.
     */
    @Transactional
    public ResultadoImportacionDto importar(byte[] zip) {
        LeccionZip.Resultado resultado;
        try {
            resultado = LeccionZip.leer(zip);
        } catch (IOException | IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se pudo leer el zip: " + e.getMessage());
        }

        List<String> nuevas = new ArrayList<>();
        List<String> actualizadas = new ArrayList<>();
        LocalDateTime ahora = LocalDateTime.now(reloj);

        for (LeccionZip.LeccionArchivo la : resultado.lecciones()) {
            Optional<Leccion> existente = lecciones.findByTitulo(la.titulo());
            Leccion leccion = existente.orElseGet(Leccion::new);
            leccion.setTitulo(la.titulo());
            leccion.setObjetivo(la.objetivo());
            leccion.setContenidoMd(la.contenidoMd() == null ? "" : la.contenidoMd());
            leccion.setFechaImportacion(ahora);
            leccion.getRecursos().clear();
            for (LeccionZip.Archivo a : la.recursos()) {
                leccion.agregarRecurso(new RecursoLeccion(a.nombre(), a.tipoContenido(), a.datos()));
            }
            lecciones.save(leccion);
            (existente.isPresent() ? actualizadas : nuevas).add(la.titulo());
        }

        if (nuevas.isEmpty() && actualizadas.isEmpty()) {
            String detalle = resultado.advertencias().isEmpty() ? "" : " " + String.join(" ", resultado.advertencias());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se importó ninguna lección." + detalle);
        }
        return new ResultadoImportacionDto(nuevas, actualizadas, resultado.advertencias());
    }

    /** Exporta todas las lecciones a un zip con la misma estructura de importación. */
    @Transactional(readOnly = true)
    public byte[] exportar() {
        List<LeccionZip.LeccionArchivo> lista = lecciones.findAllByOrderByTitulo().stream()
                .map(l -> new LeccionZip.LeccionArchivo(l.getTitulo(), l.getObjetivo(), l.getContenidoMd(),
                        l.getRecursos().stream()
                                .map(r -> new LeccionZip.Archivo(r.getNombre(), r.getTipoContenido(), r.getDatos()))
                                .toList()))
                .toList();
        try {
            return LeccionZip.escribir(lista);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "No se pudo generar el zip");
        }
    }

    @Transactional
    public String eliminar(Long id) {
        Leccion l = buscar(id);
        String titulo = l.getTitulo();
        lecciones.delete(l);
        return titulo;
    }
}
