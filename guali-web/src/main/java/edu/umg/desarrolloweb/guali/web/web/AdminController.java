package edu.umg.desarrolloweb.guali.web.web;

import edu.umg.desarrolloweb.guali.web.dto.UsuarioForm;
import edu.umg.desarrolloweb.guali.web.modelo.Usuario;
import edu.umg.desarrolloweb.guali.web.servicio.UsuarioService;
import edu.umg.desarrolloweb.guali.web.servicio.VisitaService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

/** Módulos de administración (requieren rol ADMIN, ver SeguridadConfig). */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UsuarioService usuarios;
    private final VisitaService visitas;

    public AdminController(UsuarioService usuarios, VisitaService visitas) {
        this.usuarios = usuarios;
        this.visitas = visitas;
    }

    @ModelAttribute("usuarioActual")
    public String usuarioActual(Authentication auth) {
        return auth == null ? "" : auth.getName();
    }

    @GetMapping({"", "/"})
    public String inicio() {
        return "redirect:/admin/usuarios";
    }

    // ------------------------------------------------------------ usuarios (CRUD)

    @GetMapping("/usuarios")
    public String listarUsuarios(Model model) {
        model.addAttribute("usuarios", usuarios.listar());
        model.addAttribute("seccion", "usuarios");
        return "admin/usuarios";
    }

    @GetMapping("/usuarios/nuevo")
    public String nuevoUsuario(Model model) {
        model.addAttribute("form", new UsuarioForm());
        model.addAttribute("seccion", "usuarios");
        return "admin/usuario-form";
    }

    @GetMapping("/usuarios/{id}/editar")
    public String editarUsuario(@PathVariable Long id, Model model, RedirectAttributes flash) {
        try {
            Usuario u = usuarios.buscar(id);
            UsuarioForm form = new UsuarioForm();
            form.setId(u.getId());
            form.setUsername(u.getUsername());
            form.setNombreCompleto(u.getNombreCompleto());
            form.setActivo(u.isActivo());
            model.addAttribute("form", form);
            model.addAttribute("seccion", "usuarios");
            return "admin/usuario-form";
        } catch (IllegalArgumentException e) {
            flash.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/usuarios";
        }
    }

    @PostMapping("/usuarios/guardar")
    public String guardarUsuario(@Valid @ModelAttribute("form") UsuarioForm form, BindingResult errores,
                                 Authentication auth, Model model, RedirectAttributes flash) {
        model.addAttribute("seccion", "usuarios");
        if (errores.hasErrors()) {
            return "admin/usuario-form";
        }
        try {
            Usuario u = usuarios.guardar(form, auth.getName());
            flash.addFlashAttribute("mensaje", "Usuario '" + u.getUsername() + "' guardado correctamente.");
            return "redirect:/admin/usuarios";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "admin/usuario-form";
        }
    }

    @PostMapping("/usuarios/{id}/eliminar")
    public String eliminarUsuario(@PathVariable Long id, Authentication auth, RedirectAttributes flash) {
        try {
            usuarios.eliminar(id, auth.getName());
            flash.addFlashAttribute("mensaje", "Usuario eliminado.");
        } catch (IllegalArgumentException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/usuarios";
    }

    // ------------------------------------------------------------ estadísticas

    @GetMapping("/estadisticas")
    public String estadisticas(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                               @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                               Model model) {
        LocalDate fin = hasta != null ? hasta : visitas.hoy();
        LocalDate inicio = desde != null ? desde : fin.minusDays(29);
        model.addAttribute("est", visitas.estadisticas(inicio, fin));
        model.addAttribute("seccion", "estadisticas");
        return "admin/estadisticas";
    }
}
