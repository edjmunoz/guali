package edu.umg.desarrolloweb.guali.web.modelo;

public enum EstadoHistorial {
    PENDIENTE("Pendiente"),
    COMPLETADA("Completada");

    private final String etiqueta;

    EstadoHistorial(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
