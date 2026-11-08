package edu.umg.desarrolloweb.guali.web.dto;

import jakarta.validation.constraints.NotNull;

public record AbrirLeccionRequest(@NotNull Long leccionId) {
}
