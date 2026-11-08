package edu.umg.desarrolloweb.guali.compiler;

/** Resultado de compilar: exitoso + mensajes del compilador. */
public record RespuestaCompilacion(boolean exitoso, String salida) {
}
