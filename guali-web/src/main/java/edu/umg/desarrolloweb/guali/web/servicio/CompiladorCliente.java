package edu.umg.desarrolloweb.guali.web.servicio;

import edu.umg.desarrolloweb.guali.web.dto.CompilacionRemota;
import edu.umg.desarrolloweb.guali.web.dto.EjecucionRemota;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.Map;

/** Cliente REST del microservicio guali-compiler. */
@Component
public class CompiladorCliente {

    private final RestClient cliente;

    public CompiladorCliente(@Value("${guali.compilador.url}") String url,
                             @Value("${guali.compilador.token:}") String token) {
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(Duration.ofSeconds(5));
        fabrica.setReadTimeout(Duration.ofSeconds(90));

        this.cliente = RestClient.builder()
                .baseUrl(url)
                .requestFactory(fabrica)
                .defaultHeader("X-Guali-Token", token == null ? "" : token)
                .build();
    }

    public CompilacionRemota compilar(String codigo) {
        try {
            return cliente.post().uri("/api/compilar")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("codigo", codigo))
                    .retrieve()
                    .body(CompilacionRemota.class);
        } catch (RestClientException e) {
            throw noDisponible(e);
        }
    }

    public EjecucionRemota ejecutar(String codigo) {
        try {
            return cliente.post().uri("/api/ejecutar")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("codigo", codigo))
                    .retrieve()
                    .body(EjecucionRemota.class);
        } catch (RestClientException e) {
            throw noDisponible(e);
        }
    }

    private ResponseStatusException noDisponible(RestClientException e) {
        return new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "El servicio de compilación no está disponible en este momento. Intenta de nuevo.", e);
    }
}
