package edu.umg.desarrolloweb.guali.web.dto;

/** Respuesta del microservicio guali-compiler a /api/compilar. */
public record CompilacionRemota(boolean exitoso, String salida) {
}
