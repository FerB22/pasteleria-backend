package com.pasteleria.usuario.config;

import com.pasteleria.usuario.model.Usuario;
import com.pasteleria.usuario.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Carga inicial de datos de prueba en la base de datos en memoria (H2).
 * Facilita las pruebas inmediatas en Swagger UI y Postman sin requerir registros manuales.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;

    public DataInitializer(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() == 0) {
            // 1. Usuario Administrador de la pastelería
            Usuario admin = new Usuario(
                    "11111111-1", // RUT formativo de prueba interna
                    "Alonso",
                    "Contreras",
                    "admin@milsabores.cl",
                    "admin123",
                    "+56911223344",
                    LocalDate.of(1995, 3, 15),
                    "ADMIN"
            );
            usuarioRepository.save(admin);

            // 2. Cliente mayor de 50 años (para probar el 50 % de descuento por bodas de oro)
            Usuario clienteSenior = new Usuario(
                    "15345678-5",
                    "Marta",
                    "González",
                    "marta.gonzalez@gmail.com",
                    "marta2026",
                    "+56987654321",
                    LocalDate.of(1968, 5, 20), // 58 años en 2026
                    "CLIENTE"
            );
            usuarioRepository.save(clienteSenior);

            // 3. Cliente alumno Duoc UC (para probar el beneficio de torta de cumpleaños gratis)
            Usuario alumnoDuoc = new Usuario(
                    "20123456-7",
                    "Fernando",
                    "Barra",
                    "f.barra@alumnos.duoc.cl",
                    "duoc2026",
                    "+56955667788",
                    LocalDate.of(2003, 10, 15), // Estudiante de 23 años en 2026
                    "CLIENTE"
            );
            usuarioRepository.save(alumnoDuoc);
        }
    }
}
