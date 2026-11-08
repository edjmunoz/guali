package edu.umg.desarrolloweb.guali.web.servicio;

import edu.umg.desarrolloweb.guali.web.modelo.Objetivo;

/**
 * Decide si una acción del estudiante cumple el objetivo de la lección.
 *
 * | Objetivo            | Se cumple cuando                                   |
 * |---------------------|----------------------------------------------------|
 * | COMPILAR_CON_ERROR  | Compilar y el compilador reporta al menos 1 error  |
 * | COMPILAR_EXITOSO    | Compilar con 0 errores                             |
 * | EJECUTAR_CON_ERROR  | Compila bien y la ejecución termina con error      |
 * | EJECUTAR_EXITOSO    | Compila bien y la ejecución termina sin errores    |
 */
public final class EvaluadorObjetivo {

    private EvaluadorObjetivo() {
    }

    public static boolean cumpleAlCompilar(Objetivo objetivo, boolean compilacionExitosa) {
        return switch (objetivo) {
            case COMPILAR_CON_ERROR -> !compilacionExitosa;
            case COMPILAR_EXITOSO -> compilacionExitosa;
            case EJECUTAR_CON_ERROR, EJECUTAR_EXITOSO -> false;
        };
    }

    /**
     * @param compilacionExitosa si el código compiló antes de ejecutar
     * @param ejecucionExitosa   true si terminó con código 0 y sin timeout
     */
    public static boolean cumpleAlEjecutar(Objetivo objetivo, boolean compilacionExitosa, boolean ejecucionExitosa) {
        if (!compilacionExitosa) {
            return false;
        }
        return switch (objetivo) {
            case EJECUTAR_CON_ERROR -> !ejecucionExitosa;
            case EJECUTAR_EXITOSO -> ejecucionExitosa;
            case COMPILAR_CON_ERROR, COMPILAR_EXITOSO -> false;
        };
    }
}
