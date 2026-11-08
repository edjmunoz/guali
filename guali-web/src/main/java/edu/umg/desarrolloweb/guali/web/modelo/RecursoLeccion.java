package edu.umg.desarrolloweb.guali.web.modelo;

import jakarta.persistence.*;

/** Archivo adicional de una lección (imágenes que se muestran dentro del markdown). */
@Entity
@Table(name = "recurso_leccion")
public class RecursoLeccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "leccion_id", nullable = false)
    private Leccion leccion;

    /** Ruta relativa dentro de la carpeta de la lección, ej. "imagen.jpg" o "img/diagrama.png". */
    @Column(nullable = false, length = 255)
    private String nombre;

    @Column(name = "tipo_contenido", nullable = false, length = 100)
    private String tipoContenido;

    @Column(nullable = false, columnDefinition = "LONGBLOB")
    private byte[] datos;

    public RecursoLeccion() {
    }

    public RecursoLeccion(String nombre, String tipoContenido, byte[] datos) {
        this.nombre = nombre;
        this.tipoContenido = tipoContenido;
        this.datos = datos;
    }

    public Long getId() { return id; }
    public Leccion getLeccion() { return leccion; }
    public void setLeccion(Leccion leccion) { this.leccion = leccion; }
    public String getNombre() { return nombre; }
    public String getTipoContenido() { return tipoContenido; }
    public byte[] getDatos() { return datos; }
}
