package edu.umg.desarrolloweb.guali.compiler;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Cuerpo JSON recibido: { "codigo": "..." } */
public record SolicitudCodigo(
        @NotBlank(message = "El código no puede estar vacío")
        @Size(max = 50000, message = "El código es demasiado largo")
        String codigo) {
}
