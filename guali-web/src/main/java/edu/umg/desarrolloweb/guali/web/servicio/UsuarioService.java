package edu.umg.desarrolloweb.guali.web.servicio;

import edu.umg.desarrolloweb.guali.web.dto.UsuarioForm;
import edu.umg.desarrolloweb.guali.web.modelo.Usuario;
import edu.umg.desarrolloweb.guali.web.repositorio.UsuarioRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

/** CRUD de administradores + carga de usuarios para Spring Security. */
@Service
public class UsuarioService implements UserDetailsService {

    private final UsuarioRepository repositorio;
    private final PasswordEncoder encoder;
    private final Clock reloj;

    public UsuarioService(UsuarioRepository repositorio, PasswordEncoder encoder, Clock reloj) {
        this.repositorio = repositorio;
        this.encoder = encoder;
        this.reloj = reloj;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario u = repositorio.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
        return User.withUsername(u.getUsername())
                .password(u.getPasswordHash())
                .roles("ADMIN")
                .disabled(!u.isActivo())
                .build();
    }

    @Transactional(readOnly = true)
    public List<Usuario> listar() {
        return repositorio.findAllByOrderByUsernameAsc();
    }

    @Transactional(readOnly = true)
    public Usuario buscar(Long id) {
        return repositorio.findById(id).orElseThrow(() -> new IllegalArgumentException("El usuario no existe"));
    }

    @Transactional(readOnly = true)
    public long contar() {
        return repositorio.count();
    }

    /**
     * Crea o actualiza. Lanza IllegalArgumentException con un mensaje para el usuario si algo no es válido.
     * @param usuarioActual username del administrador que hace el cambio
     */
    @Transactional
    public Usuario guardar(UsuarioForm form, String usuarioActual) {
        String username = form.getUsername().trim();
        String password = form.getPassword() == null ? "" : form.getPassword();

        Usuario u;
        if (form.esNuevo()) {
            if (repositorio.existsByUsername(username)) {
                throw new IllegalArgumentException("Ya existe un usuario con ese nombre");
            }
            if (password.length() < 8) {
                throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres");
            }
            u = new Usuario();
            u.setFechaCreacion(LocalDateTime.now(reloj));
        } else {
            u = buscar(form.getId());
            if (!u.getUsername().equals(username) && repositorio.existsByUsername(username)) {
                throw new IllegalArgumentException("Ya existe un usuario con ese nombre");
            }
            if (!password.isEmpty() && password.length() < 8) {
                throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres");
            }
            if (u.getUsername().equals(usuarioActual) && !form.isActivo()) {
                throw new IllegalArgumentException("No puedes desactivar tu propio usuario");
            }
            if (u.isActivo() && !form.isActivo() && repositorio.countByActivoTrue() <= 1) {
                throw new IllegalArgumentException("Debe quedar al menos un administrador activo");
            }
        }
        u.setUsername(username);
        u.setNombreCompleto(form.getNombreCompleto().trim());
        u.setActivo(form.isActivo());
        if (!password.isEmpty()) {
            u.setPasswordHash(encoder.encode(password));
        }
        return repositorio.save(u);
    }

    @Transactional
    public void eliminar(Long id, String usuarioActual) {
        Usuario u = buscar(id);
        if (u.getUsername().equals(usuarioActual)) {
            throw new IllegalArgumentException("No puedes eliminar tu propio usuario");
        }
        if (u.isActivo() && repositorio.countByActivoTrue() <= 1) {
            throw new IllegalArgumentException("Debe quedar al menos un administrador activo");
        }
        repositorio.delete(u);
    }

    /** Crea el administrador inicial si la tabla está vacía. */
    @Transactional
    public boolean crearAdminInicialSiNoHay(String username, String password) {
        if (repositorio.count() > 0) {
            return false;
        }
        Usuario u = new Usuario();
        u.setUsername(username);
        u.setNombreCompleto("Administrador");
        u.setPasswordHash(encoder.encode(password));
        u.setActivo(true);
        u.setFechaCreacion(LocalDateTime.now(reloj));
        repositorio.save(u);
        return true;
    }
}
