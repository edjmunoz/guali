package edu.umg.desarrolloweb.guali.web.config;

import edu.umg.desarrolloweb.guali.web.servicio.LeccionService;
import edu.umg.desarrolloweb.guali.web.servicio.UsuarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;

/** Al arrancar: crea el administrador inicial y carga lecciones de ejemplo si la base está vacía. */
@Component
public class DatosIniciales implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DatosIniciales.class);

    private final UsuarioService usuarios;
    private final LeccionService lecciones;
    private final String adminUsuario;
    private final String adminPassword;
    private final boolean cargarEjemplos;

    public DatosIniciales(UsuarioService usuarios, LeccionService lecciones,
                          @Value("${guali.admin.usuario}") String adminUsuario,
                          @Value("${guali.admin.password}") String adminPassword,
                          @Value("${guali.lecciones-ejemplo:true}") boolean cargarEjemplos) {
        this.usuarios = usuarios;
        this.lecciones = lecciones;
        this.adminUsuario = adminUsuario;
        this.adminPassword = adminPassword;
        this.cargarEjemplos = cargarEjemplos;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (usuarios.crearAdminInicialSiNoHay(adminUsuario, adminPassword)) {
            log.info("Se creó el administrador inicial '{}'. Cambie la contraseña desde Administración.", adminUsuario);
        }
        if (cargarEjemplos && lecciones.contar() == 0) {
            ClassPathResource zip = new ClassPathResource("lecciones-ejemplo.zip");
            if (zip.exists()) {
                try (InputStream in = zip.getInputStream()) {
                    var r = lecciones.importar(in.readAllBytes());
                    log.info("Lecciones de ejemplo importadas: {}", r.nuevas());
                }
            }
        }
    }
}
