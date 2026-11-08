package edu.umg.desarrolloweb.guali.compiler;

import edu.umg.guali.compiler.CompileResult;
import edu.umg.guali.compiler.ExecuteResult;
import edu.umg.guali.compiler.GualiDev;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Envuelve la API de guali-dev.
 *
 * guali-dev guarda los archivos .java/.class en una carpeta temporal compartida
 * (java.io.tmpdir/guali-dev) y el código "compacto" de Java 25 siempre se llama
 * "app25". Si dos estudiantes compilan al mismo tiempo, uno sobrescribiría la
 * clase del otro. Por eso:
 *   1) Cada operación se protege con un candado (una a la vez).
 *   2) "Ejecutar" vuelve a compilar el código y ejecuta en la misma sección
 *      protegida, así siempre se ejecuta el código del estudiante correcto.
 */
@Service
public class CompiladorService {

    private static final Logger log = LoggerFactory.getLogger(CompiladorService.class);

    private final ReentrantLock candado = new ReentrantLock(true);
    private final String jdkHome;
    private final int timeoutSegundos;
    private final int maxCaracteres;

    public CompiladorService(@Value("${guali.jdk-home:}") String jdkHome,
                             @Value("${guali.timeout-segundos:10}") int timeoutSegundos,
                             @Value("${guali.max-caracteres-salida:20000}") int maxCaracteres) {
        this.jdkHome = (jdkHome == null || jdkHome.isBlank()) ? System.getProperty("java.home") : jdkHome;
        this.timeoutSegundos = timeoutSegundos;
        this.maxCaracteres = maxCaracteres;

        Path javac = Path.of(this.jdkHome, "bin", esWindows() ? "javac.exe" : "javac");
        if (Files.exists(javac)) {
            log.info("guali-dev usará el JDK en: {}", this.jdkHome);
        } else {
            log.warn("No se encontró javac en {}. Configure GUALI_JDK_HOME con la ruta de un JDK 25.", javac);
        }
    }

    public RespuestaCompilacion compilar(String codigo) {
        bloquear();
        try {
            CompileResult cr = GualiDev.compile(codigo, jdkHome);
            return new RespuestaCompilacion(cr.isExitoso(), recortar(cr.getSalida()));
        } finally {
            candado.unlock();
        }
    }

    public RespuestaEjecucion ejecutar(String codigo) {
        bloquear();
        try {
            CompileResult cr = GualiDev.compile(codigo, jdkHome);
            if (!cr.isExitoso()) {
                return new RespuestaEjecucion(false, recortar(cr.getSalida()), false, "", "", false);
            }
            ExecuteResult er = GualiDev.execute(cr.getNombreClase(), jdkHome, timeoutSegundos);
            return new RespuestaEjecucion(true, "",
                    er.isExitoso(),
                    recortar(er.getSalida()),
                    recortar(er.getErrores()),
                    er.isTimeoutAlcanzado());
        } finally {
            candado.unlock();
        }
    }

    public int getTimeoutSegundos() {
        return timeoutSegundos;
    }

    /** Espera su turno como máximo 45 s; si no, responde 503 (servidor ocupado). */
    private void bloquear() {
        try {
            if (!candado.tryLock(45, TimeUnit.SECONDS)) {
                throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                        "El servidor de compilación está ocupado, intenta de nuevo.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Operación interrumpida");
        }
    }

    private String recortar(String texto) {
        if (texto == null) {
            return "";
        }
        if (texto.length() <= maxCaracteres) {
            return texto;
        }
        return texto.substring(0, maxCaracteres) + "\n... (salida recortada)";
    }

    private static boolean esWindows() {
        return System.getProperty("os.name", "").toLowerCase().contains("win");
    }
}
