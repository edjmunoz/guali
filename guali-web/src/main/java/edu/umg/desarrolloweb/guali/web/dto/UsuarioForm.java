package edu.umg.desarrolloweb.guali.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Formulario de creación / edición de administradores. */
public class UsuarioForm {

    private Long id;

    @NotBlank(message = "El usuario es obligatorio")
    @Size(min = 3, max = 50, message = "Entre 3 y 50 caracteres")
    @Pattern(regexp = "[A-Za-z0-9._-]*", message = "Solo letras, números, punto, guion y guion bajo")
    private String username;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 120, message = "Máximo 120 caracteres")
    private String nombreCompleto;

    /** Obligatoria al crear; al editar, vacía = no cambiar. */
    @Size(max = 72, message = "Máximo 72 caracteres")
    private String password;

    private boolean activo = true;

    public boolean esNuevo() {
        return id == null;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
