package com.pasteleria.usuario;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Punto de entrada principal del backend integrado de Pastelería Mil Sabores.
 * Arquitectura: Spring Boot 3.3.4 con Java 21.
 * Se amplía el escaneo desde la raíz "com.pasteleria" para cargar los módulos
 * de usuarios, productos y pedidos cuando se integren en la rama principal.
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.pasteleria")
@EntityScan(basePackages = "com.pasteleria")
@EnableJpaRepositories(basePackages = "com.pasteleria")
public class UsuarioApplication {

    public static void main(String[] args) {
        SpringApplication.run(UsuarioApplication.class, args);
    }
}
