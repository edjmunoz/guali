package edu.umg.desarrolloweb.guali.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

public record PreferenciaDto(
        @Min(10) @Max(32) int tamanoFuente,
        @Pattern(regexp = "claro|oscuro") String tema) {
}
