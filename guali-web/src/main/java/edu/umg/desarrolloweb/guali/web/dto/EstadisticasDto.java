package edu.umg.desarrolloweb.guali.web.dto;

import java.time.LocalDate;
import java.util.List;

public record EstadisticasDto(LocalDate desde, LocalDate hasta, long visitas, long visitantesUnicos,
                              long leccionesAbiertas, long leccionesCompletadas, List<Dia> dias, long maximoDia) {

    public record Dia(LocalDate dia, long visitas, long visitantesUnicos, int porcentaje) {
    }
}
