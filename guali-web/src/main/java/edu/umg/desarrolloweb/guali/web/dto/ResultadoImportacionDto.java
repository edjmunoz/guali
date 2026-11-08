package edu.umg.desarrolloweb.guali.web.dto;

import java.util.List;

public record ResultadoImportacionDto(List<String> nuevas, List<String> actualizadas, List<String> advertencias) {
}
