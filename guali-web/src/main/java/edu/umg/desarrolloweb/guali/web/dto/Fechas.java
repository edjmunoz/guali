package edu.umg.desarrolloweb.guali.web.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class Fechas {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private Fechas() {
    }

    public static String formatear(LocalDateTime fecha) {
        return fecha == null ? "" : FORMATO.format(fecha);
    }
}
