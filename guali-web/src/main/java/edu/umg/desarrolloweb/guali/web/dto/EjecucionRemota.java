package edu.umg.desarrolloweb.guali.web.dto;

/** Respuesta del microservicio guali-compiler a /api/ejecutar. */
public record EjecucionRemota(boolean compilacionExitosa, String salidaCompilacion, boolean exitoso,
                              String salida, String errores, boolean timeout) {
}
