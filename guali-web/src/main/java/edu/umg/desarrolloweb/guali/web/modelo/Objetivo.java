package edu.umg.desarrolloweb.guali.web.modelo;

/** Tipos de objetivo definidos en config.txt de cada lección. */
public enum Objetivo {
    COMPILAR_CON_ERROR("Compila el código y obtén al menos un error de compilación"),
    COMPILAR_EXITOSO("Compila el código sin errores"),
    EJECUTAR_CON_ERROR("Compila sin errores y provoca un error al ejecutar"),
    EJECUTAR_EXITOSO("Compila y ejecuta el programa sin errores");

    private final String descripcion;

    Objetivo(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
