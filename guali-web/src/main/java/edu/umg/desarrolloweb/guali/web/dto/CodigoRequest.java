package edu.umg.desarrolloweb.guali.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** sesionId es opcional: se puede compilar sin tener una lección abierta. */
public record CodigoRequest(
        Long sesionId,
        @NotBlank(message = "El editor está vacío, escribe tu código primero.")
        @Size(max = 50000, message = "El código es demasiado largo (máx. 50 000 caracteres).")
        String codigo) {
}
