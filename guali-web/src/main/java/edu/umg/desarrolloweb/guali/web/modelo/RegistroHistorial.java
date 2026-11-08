package edu.umg.desarrolloweb.guali.web.modelo;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Un intento de lección: se crea al abrir la lección y se cierra cuando el
 * objetivo se cumple (COMPLETADA) o cuando el estudiante la cierra/abandona
 * (PENDIENTE). Guarda el título como texto para conservar el historial aunque
 * la lección se elimine después.
 */
@Entity
@Table(name = "historial", indexes = @Index(name = "idx_historial_cliente", columnList = "cliente_id"))
public class RegistroHistorial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Identificador anónimo del navegador del estudiante (cookie guali_cid). */
    @Column(name = "cliente_id", nullable = false, length = 36)
    private String clienteId;

    @Column(name = "leccion_id")
    private Long leccionId;

    @Column(name = "leccion_titulo", nullable = false, length = 200)
    private String leccionTitulo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Objetivo objetivo;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoHistorial estado = EstadoHistorial.PENDIENTE;

    @Column(name = "intentos_compilar", nullable = false)
    private int intentosCompilar;

    @Column(name = "intentos_ejecutar", nullable = false)
    private int intentosEjecutar;

    public boolean estaAbierto() {
        return fechaFin == null;
    }

    public Long getId() { return id; }
    public String getClienteId() { return clienteId; }
    public void setClienteId(String clienteId) { this.clienteId = clienteId; }
    public Long getLeccionId() { return leccionId; }
    public void setLeccionId(Long leccionId) { this.leccionId = leccionId; }
    public String getLeccionTitulo() { return leccionTitulo; }
    public void setLeccionTitulo(String leccionTitulo) { this.leccionTitulo = leccionTitulo; }
    public Objetivo getObjetivo() { return objetivo; }
    public void setObjetivo(Objetivo objetivo) { this.objetivo = objetivo; }
    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDateTime fechaInicio) { this.fechaInicio = fechaInicio; }
    public LocalDateTime getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDateTime fechaFin) { this.fechaFin = fechaFin; }
    public EstadoHistorial getEstado() { return estado; }
    public void setEstado(EstadoHistorial estado) { this.estado = estado; }
    public int getIntentosCompilar() { return intentosCompilar; }
    public void sumarIntentoCompilar() { this.intentosCompilar++; }
    public int getIntentosEjecutar() { return intentosEjecutar; }
    public void sumarIntentoEjecutar() { this.intentosEjecutar++; }
}
