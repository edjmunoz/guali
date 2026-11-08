package edu.umg.desarrolloweb.guali.compiler;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Solo acepta peticiones a /api/** que traigan el encabezado X-Guali-Token
 * con el secreto compartido. Evita que alguien invoque el compilador
 * directamente sin pasar por guali-web.
 */
@Component
public class TokenInternoFiltro extends OncePerRequestFilter {

    public static final String ENCABEZADO = "X-Guali-Token";

    private final String token;

    public TokenInternoFiltro(@Value("${guali.token-interno:}") String token) {
        this.token = token;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return token == null || token.isBlank() || !request.getRequestURI().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String recibido = request.getHeader(ENCABEZADO);
        boolean valido = recibido != null && MessageDigest.isEqual(
                recibido.getBytes(StandardCharsets.UTF_8), token.getBytes(StandardCharsets.UTF_8));
        if (!valido) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Token interno inválido");
            return;
        }
        chain.doFilter(request, response);
    }
}
