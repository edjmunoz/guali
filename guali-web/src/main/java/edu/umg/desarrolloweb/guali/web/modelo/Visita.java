package edu.umg.desarrolloweb.guali.web.modelo;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Una visita a la página principal. Sirve para las estadísticas de uso. */
@Entity
@Table(name = "visita", indexes = @Index(name = "idx_visita_dia", columnList = "dia"))
public class Visita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cliente_id", nullable = false, length = 36)
    private String clienteId;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    /** Día de la visita (se guarda aparte para agrupar fácilmente). */
    @Column(nullable = false)
    private LocalDate dia;

    @Column(name = "agente_usuario", length = 255)
    private String agenteUsuario;

    public Visita() {
    }

    public Visita(String clienteId, LocalDateTime fechaHora, String agenteUsuario) {
        this.clienteId = clienteId;
        this.fechaHora = fechaHora;
        this.dia = fechaHora.toLocalDate();
        if (agenteUsuario != null && agenteUsuario.length() > 255) {
            agenteUsuario = agenteUsuario.substring(0, 255);
        }
        this.agenteUsuario = agenteUsuario;
    }

    public Long getId() { return id; }
    public String getClienteId() { return clienteId; }
    public LocalDateTime getFechaHora() { return fechaHora; }
    public LocalDate getDia() { return dia; }
    public String getAgenteUsuario() { return agenteUsuario; }
}
