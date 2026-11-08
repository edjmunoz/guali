package edu.umg.desarrolloweb.guali.web.dto;

/**
 * @param objetivoCumplido true si con esta acción se completó la lección
 * @param estado           estado actual del registro de historial (o null si no hay lección abierta)
 */
public record ResultadoCompilarDto(boolean exitoso, String salida, boolean objetivoCumplido, String estado) {
}
