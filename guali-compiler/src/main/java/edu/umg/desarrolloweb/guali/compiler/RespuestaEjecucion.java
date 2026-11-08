package edu.umg.desarrolloweb.guali.compiler;

/**
 * Resultado de ejecutar.
 * El código se vuelve a compilar antes de ejecutar (ver CompiladorService),
 * por eso también se devuelve el resultado de la compilación.
 */
public record RespuestaEjecucion(
        boolean compilacionExitosa,
        String salidaCompilacion,
        boolean exitoso,
        String salida,
        String errores,
        boolean timeout) {
}
