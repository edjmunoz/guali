package edu.umg.desarrolloweb.guali.web.servicio;

import org.junit.jupiter.api.Test;

import static edu.umg.desarrolloweb.guali.web.modelo.Objetivo.*;
import static org.junit.jupiter.api.Assertions.*;

class EvaluadorObjetivoTest {

    @Test
    void compilarConError() {
        assertTrue(EvaluadorObjetivo.cumpleAlCompilar(COMPILAR_CON_ERROR, false));
        assertFalse(EvaluadorObjetivo.cumpleAlCompilar(COMPILAR_CON_ERROR, true));
    }

    @Test
    void compilarExitoso() {
        assertTrue(EvaluadorObjetivo.cumpleAlCompilar(COMPILAR_EXITOSO, true));
        assertFalse(EvaluadorObjetivo.cumpleAlCompilar(COMPILAR_EXITOSO, false));
    }

    @Test
    void ejecutarConError() {
        assertTrue(EvaluadorObjetivo.cumpleAlEjecutar(EJECUTAR_CON_ERROR, true, false));
        assertFalse(EvaluadorObjetivo.cumpleAlEjecutar(EJECUTAR_CON_ERROR, false, false));
        assertFalse(EvaluadorObjetivo.cumpleAlEjecutar(EJECUTAR_CON_ERROR, true, true));
    }

    @Test
    void ejecutarExitoso() {
        assertTrue(EvaluadorObjetivo.cumpleAlEjecutar(EJECUTAR_EXITOSO, true, true));
        assertFalse(EvaluadorObjetivo.cumpleAlEjecutar(EJECUTAR_EXITOSO, true, false));
        assertFalse(EvaluadorObjetivo.cumpleAlCompilar(EJECUTAR_EXITOSO, true));
    }
}
