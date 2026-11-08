package edu.umg.desarrolloweb.guali.web.web;

import edu.umg.desarrolloweb.guali.web.config.ClienteIdFiltro;
import edu.umg.desarrolloweb.guali.web.servicio.VisitaService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;

/** Controlador MVC de las páginas públicas (principal y login). */
@Controller
public class PaginaController {

    private final VisitaService visitas;

    public PaginaController(VisitaService visitas) {
        this.visitas = visitas;
    }

    @GetMapping("/")
    public String inicio(@RequestAttribute(ClienteIdFiltro.ATRIBUTO) String clienteId,
                         Authentication auth, HttpServletRequest request, Model model) {
        boolean esAdmin = esAdmin(auth);
        if (!esAdmin) {
            visitas.registrar(clienteId, request.getHeader("User-Agent"));
        }
        model.addAttribute("esAdmin", esAdmin);
        model.addAttribute("usuario", esAdmin ? auth.getName() : null);
        return "index";
    }

    @GetMapping("/login")
    public String login(Authentication auth) {
        if (esAdmin(auth)) {
            return "redirect:/";
        }
        return "login";
    }

    static boolean esAdmin(Authentication auth) {
        return auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)
                && auth.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }
}
