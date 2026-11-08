package edu.umg.desarrolloweb.guali.web.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Los estudiantes no inician sesión, así que cada navegador recibe un
 * identificador anónimo (cookie "guali_cid", 1 año). Con él se guardan su
 * historial, sus preferencias y se cuentan los visitantes únicos.
 */
@Component
public class ClienteIdFiltro extends OncePerRequestFilter {

    public static final String COOKIE = "guali_cid";
    public static final String ATRIBUTO = "clienteId";
    private static final Pattern UUID_VALIDO = Pattern.compile("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String clienteId = leerCookie(request);
        if (clienteId == null) {
            clienteId = UUID.randomUUID().toString();
            ResponseCookie cookie = ResponseCookie.from(COOKIE, clienteId)
                    .path("/")
                    .httpOnly(true)
                    .secure(request.isSecure())
                    .sameSite("Lax")
                    .maxAge(Duration.ofDays(365))
                    .build();
            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        }
        request.setAttribute(ATRIBUTO, clienteId);
        chain.doFilter(request, response);
    }

    private static String leerCookie(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        for (Cookie c : request.getCookies()) {
            if (COOKIE.equals(c.getName()) && c.getValue() != null && UUID_VALIDO.matcher(c.getValue()).matches()) {
                return c.getValue();
            }
        }
        return null;
    }
}
