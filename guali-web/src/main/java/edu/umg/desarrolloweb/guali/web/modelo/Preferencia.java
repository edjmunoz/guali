package edu.umg.desarrolloweb.guali.web.modelo;

import jakarta.persistence.*;

/** Preferencias de configuración (tamaño de fuente y tema) por navegador. */
@Entity
@Table(name = "preferencia")
public class Preferencia {

    @Id
    @Column(name = "cliente_id", length = 36)
    private String clienteId;

    @Column(name = "tamano_fuente", nullable = false)
    private int tamanoFuente = 16;

    @Column(nullable = false, length = 10)
    private String tema = "claro";

    public Preferencia() {
    }

    public Preferencia(String clienteId) {
        this.clienteId = clienteId;
    }

    public String getClienteId() { return clienteId; }
    public int getTamanoFuente() { return tamanoFuente; }
    public void setTamanoFuente(int tamanoFuente) { this.tamanoFuente = tamanoFuente; }
    public String getTema() { return tema; }
    public void setTema(String tema) { this.tema = tema; }
}
