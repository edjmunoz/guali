package edu.umg.desarrolloweb.guali.web.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

@Configuration
public class AppConfig {

    /** Reloj con la zona horaria de Guatemala para todas las fechas del sistema. */
    @Bean
    public Clock reloj(@Value("${guali.zona-horaria:America/Guatemala}") String zona) {
        return Clock.system(ZoneId.of(zona));
    }
}
