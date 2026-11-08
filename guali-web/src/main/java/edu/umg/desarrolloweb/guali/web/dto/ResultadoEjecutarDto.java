package edu.umg.desarrolloweb.guali.web.dto;

public record ResultadoEjecutarDto(boolean compilacionExitosa, String salidaCompilacion, boolean exitoso,
                                   String salida, String errores, boolean timeout,
                                   boolean objetivoCumplido, String estado) {
}
