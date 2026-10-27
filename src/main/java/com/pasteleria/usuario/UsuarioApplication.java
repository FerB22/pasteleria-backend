package com.pasteleria.usuario;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada principal del microservicio de Usuarios y Beneficios.
 * Arquitectura: Spring Boot 3.3.4 con Java 21.
 */
@SpringBootApplication
public class UsuarioApplication {

    public static void main(String[] args) {
        SpringApplication.run(UsuarioApplication.class, args);
    }
}
