package edu.umg.desarrolloweb.guali.web.servicio;

import edu.umg.desarrolloweb.guali.web.modelo.Objetivo;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LeccionZipTest {

    @Test
    void leeConfigTxt() {
        assertEquals(Objetivo.COMPILAR_EXITOSO, LeccionZip.leerObjetivo("objetivo=COMPILAR_EXITOSO"));
        assertEquals(Objetivo.EJECUTAR_CON_ERROR, LeccionZip.leerObjetivo("﻿  objetivo = ejecutar_con_error \r\n"));
        assertNull(LeccionZip.leerObjetivo("objetivo=OTRA_COSA"));
        assertNull(LeccionZip.leerObjetivo(""));
    }

    @Test
    void leeBundleDeEjemploYLoVuelveAEscribir() throws Exception {
        byte[] zip;
        try (InputStream in = getClass().getResourceAsStream("/lecciones-ejemplo.zip")) {
            assertNotNull(in);
            zip = in.readAllBytes();
        }
        LeccionZip.Resultado r = LeccionZip.leer(zip);
        assertEquals(5, r.lecciones().size());
        assertEquals("leccion-01-hola-mundo", r.lecciones().get(0).titulo());
        assertEquals(1, r.lecciones().get(0).recursos().size());

        // Exportar e importar de nuevo debe dar lo mismo
        LeccionZip.Resultado otra = LeccionZip.leer(LeccionZip.escribir(r.lecciones()));
        assertEquals(r.lecciones().size(), otra.lecciones().size());
        List<String> titulos = otra.lecciones().stream().map(LeccionZip.LeccionArchivo::titulo).toList();
        assertTrue(titulos.contains("leccion-04-errores-de-ejecucion"));
    }
}
