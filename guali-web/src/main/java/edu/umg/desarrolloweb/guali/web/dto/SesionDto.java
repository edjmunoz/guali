package edu.umg.desarrolloweb.guali.web.dto;

/** Respuesta al abrir una lección: id del registro de historial + detalle de la lección. */
public record SesionDto(Long sesionId, LeccionDetalleDto leccion) {
}
