package edu.umg.desarrolloweb.guali.compiler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Microservicio de compilación.
 * Expone una API REST interna que usa guali-dev.jar para compilar y ejecutar
 * el código que escribe el estudiante. No tiene base de datos y no se publica
 * hacia internet: solo guali-web puede invocarlo.
 */
@SpringBootApplication
public class GualiCompilerApplication {

    public static void main(String[] args) {
        SpringApplication.run(GualiCompilerApplication.class, args);
    }
}
