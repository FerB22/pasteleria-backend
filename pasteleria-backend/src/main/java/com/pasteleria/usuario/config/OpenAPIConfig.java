package com.pasteleria.usuario.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuración global de OpenAPI 3 / Swagger para la API RESTful.
 * Cumple con los requerimientos del indicador IE3.2.1 e IE3.2.3 de DSY1104.
 * Incluye esquema de autorización JWT Bearer para pruebas interactivas en Swagger UI.
 */
@Configuration
public class OpenAPIConfig {

    private static final String SECURITY_SCHEME_NAME = "Bearer Authentication";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Pastelería Mil Sabores — API RESTful v1")
                        .version("1.0.0")
                        .description("API RESTful para la plataforma de comercio electrónico de Pastelería Mil Sabores. "
                                + "Gestiona autenticación mediante JWT, roles de usuario (ADMIN, VENDEDOR, CLIENTE), "
                                + "validaciones de negocio bajo norma chilena (Módulo 11) y beneficios comerciales.")
                        .contact(new Contact()
                                .name("Equipo de Desarrollo Full Stack — Pastelería Mil Sabores")
                                .email("contacto@milsabores.cl")
                                .url("https://github.com/FerB22/pasteleria-backend"))
                        .license(new License()
                                .name("Uso Académico Duoc UC — DSY1104")
                                .url("https://www.duoc.cl")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Servidor de Desarrollo Local (Spring Boot en puerto 8080)")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Ingrese el token JWT obtenido en el endpoint de autenticación (/api/v1/usuarios/login). "
                                        + "Ejemplo: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")));
    }
}
