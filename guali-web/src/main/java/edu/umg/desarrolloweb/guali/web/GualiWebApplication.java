package edu.umg.desarrolloweb.guali.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Aplicación web principal de Guali Dev (patrón MVC).
 *  - Vistas Thymeleaf + HTML/CSS/JavaScript.
 *  - API REST consumida por el JavaScript de la página principal.
 *  - Persistencia con JPA sobre MariaDB.
 *  - Spring Security para el rol Administrador.
 *  - Consume el microservicio guali-compiler por REST.
 */
@SpringBootApplication
public class GualiWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(GualiWebApplication.class, args);
    }
}
