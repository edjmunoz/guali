package edu.umg.desarrolloweb.guali.web.dto;

public record HistorialDto(Long id, String leccion, String fechaInicio, String fechaFin, String estado,
                           int intentosCompilar, int intentosEjecutar) {
}
