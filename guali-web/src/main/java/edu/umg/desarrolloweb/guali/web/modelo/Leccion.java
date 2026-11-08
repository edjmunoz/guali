package edu.umg.desarrolloweb.guali.web.modelo;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Una lección importada desde un bundle .zip (carpeta + leccion.md + config.txt + imágenes). */
@Entity
@Table(name = "leccion")
public class Leccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nombre de la carpeta = título de la lección. */
    @Column(nullable = false, unique = true, length = 200)
    private String titulo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Objetivo objetivo;

    /** Contenido del archivo leccion.md */
    @Column(name = "contenido_md", nullable = false, columnDefinition = "LONGTEXT")
    private String contenidoMd;

    @Column(name = "fecha_importacion", nullable = false)
    private LocalDateTime fechaImportacion;

    @OneToMany(mappedBy = "leccion", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<RecursoLeccion> recursos = new ArrayList<>();

    public void agregarRecurso(RecursoLeccion recurso) {
        recurso.setLeccion(this);
        recursos.add(recurso);
    }

    public Long getId() { return id; }
    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public Objetivo getObjetivo() { return objetivo; }
    public void setObjetivo(Objetivo objetivo) { this.objetivo = objetivo; }
    public String getContenidoMd() { return contenidoMd; }
    public void setContenidoMd(String contenidoMd) { this.contenidoMd = contenidoMd; }
    public LocalDateTime getFechaImportacion() { return fechaImportacion; }
    public void setFechaImportacion(LocalDateTime fechaImportacion) { this.fechaImportacion = fechaImportacion; }
    public List<RecursoLeccion> getRecursos() { return recursos; }
}
