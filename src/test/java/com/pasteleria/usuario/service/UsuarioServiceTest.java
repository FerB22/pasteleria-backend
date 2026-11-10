package com.pasteleria.usuario.service;

import com.pasteleria.usuario.model.Usuario;
import com.pasteleria.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias para UsuarioService.
 * Verifica la precisión del Módulo 11 y el cálculo de descuentos comerciales.
 */
@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuarioSenior;
    private Usuario usuarioDuoc;

    @BeforeEach
    void setUp() {
        usuarioSenior = new Usuario(
                "15345678-K",
                "Marta",
                "González",
                "marta@gmail.com",
                "clave123",
                "+56911112222",
                LocalDate.of(1970, 1, 1), // 56 años
                "CLIENTE"
        );
        usuarioSenior.setId(1L);

        usuarioDuoc = new Usuario(
                "20123456-5",
                "Fernando",
                "Barra",
                "f.barra@alumnos.duoc.cl",
                "claveDuoc",
                "+56933334444",
                LocalDate.of(2003, 5, 10), // 23 años
                "CLIENTE"
        );
        usuarioDuoc.setId(2L);
    }

    @Test
    @DisplayName("Debe validar correctamente un RUT chileno válido con Módulo 11")
    void testValidarRutChilenoValido() {
        assertTrue(usuarioService.validarRutChileno("15.345.678-K"));
        assertTrue(usuarioService.validarRutChileno("20123456-5"));
    }

    @Test
    @DisplayName("Debe rechazar RUTs con formato inválido, cadenas en blanco o secuencias repetitivas")
    void testValidarRutChilenoInvalido() {
        assertFalse(usuarioService.validarRutChileno(null));
        assertFalse(usuarioService.validarRutChileno("   "));
        assertFalse(usuarioService.validarRutChileno("11.111.111-1")); // Secuencia repetitiva
        assertFalse(usuarioService.validarRutChileno("12.345.678-0")); // Dígito verificador erróneo
    }

    @Test
    @DisplayName("Debe validar correos legítimos con estructura completa y dominio válido")
    void testValidarEmailValido() {
        assertTrue(usuarioService.validarEmail("marta.gonzalez@gmail.com"));
        assertTrue(usuarioService.validarEmail("f.barra@alumnos.duoc.cl"));
        assertTrue(usuarioService.validarEmail("contacto@milsabores.cl"));
    }

    @Test
    @DisplayName("Debe rechazar correos sintéticos como f@j.j.cl según la política de máxima restricción")
    void testValidarEmailInvalido() {
        assertFalse(usuarioService.validarEmail(null));
        assertFalse(usuarioService.validarEmail("   "));
        assertFalse(usuarioService.validarEmail("f@j.j.cl")); // Caso exacto probado por el profesor
        assertFalse(usuarioService.validarEmail("a@b.c"));
        assertFalse(usuarioService.validarEmail("usuario@j.cl")); // Dominio de 1 letra
        assertFalse(usuarioService.validarEmail("usuario..invalido@gmail.com")); // Puntos consecutivos
    }

    @Test
    @DisplayName("Debe aplicar 50 % de descuento a clientes con 50 años o más")
    void testCalcularBeneficiosSenior() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioSenior));

        Map<String, Object> beneficios = usuarioService.calcularBeneficios(1L, null);

        assertEquals(50, beneficios.get("descuentoPorcentaje"));
        assertTrue(beneficios.get("motivoDescuento").toString().contains("50 aniversario"));
    }

    @Test
    @DisplayName("Debe aplicar 10 % de descuento si ingresa cupón FELICES50")
    void testCalcularBeneficiosCupon() {
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuarioDuoc));

        Map<String, Object> beneficios = usuarioService.calcularBeneficios(2L, "FELICES50");

        assertEquals(10, beneficios.get("descuentoPorcentaje"));
        assertEquals("Torta gratis de cumpleaños aplicable en el mes natal", beneficios.get("beneficioDuocCumpleanos"));
    }

    @Test
    @DisplayName("Debe rechazar el registro de usuarios con fecha de nacimiento futura")
    void testRechazarFechaNacimientoFutura() {
        Usuario usuarioFuturo = new Usuario(
                "15345678-K",
                "Juan",
                "Pérez",
                "juan.perez@gmail.com",
                "claveSegura2026",
                "+56911223344",
                LocalDate.now().plusDays(5),
                "CLIENTE"
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                usuarioService.registrarUsuario(usuarioFuturo)
        );
        assertTrue(ex.getMessage().contains("futura"));
    }

    @Test
    @DisplayName("Debe rechazar el registro con contraseña menor a 6 caracteres o en blanco")
    void testRechazarPasswordDebil() {
        Usuario usuarioClaveCorta = new Usuario(
                "15345678-K",
                "Juan",
                "Pérez",
                "juan.perez@gmail.com",
                "123",
                "+56911223344",
                LocalDate.of(1995, 3, 20),
                "CLIENTE"
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                usuarioService.registrarUsuario(usuarioClaveCorta)
        );
        assertTrue(ex.getMessage().contains("al menos 6 caracteres"));
    }

    @Test
    @DisplayName("Debe reconocer correos institucionales de Duoc UC para docentes y estudiantes")
    void testReconocerDominiosInstitucionalesDuoc() {
        Usuario docente = new Usuario(
                "15345678-K",
                "Profesor",
                "Duoc",
                "docente@profesor.duoc.cl",
                "claveDocente2026",
                "+56911223344",
                LocalDate.of(1980, 5, 12),
                "CLIENTE"
        );
        assertTrue(docente.esUsuarioDuoc());
    }

    @Test
    @DisplayName("Debe autenticar comparando la clave contra el hash BCrypt")
    void testAutenticarConHash() {
        usuarioDuoc.setPassword("$2a$10$hashSimulado");
        when(usuarioRepository.findByEmail("f.barra@alumnos.duoc.cl")).thenReturn(Optional.of(usuarioDuoc));
        when(passwordEncoder.matches("duoc2026", "$2a$10$hashSimulado")).thenReturn(true);

        assertEquals(usuarioDuoc, usuarioService.autenticar("F.Barra@alumnos.duoc.cl", "duoc2026"));
    }

    @Test
    @DisplayName("Clave errónea y correo inexistente responden el mismo mensaje")
    void testAutenticarNoRevelaSiExisteElCorreo() {
        usuarioDuoc.setPassword("$2a$10$hashSimulado");
        when(usuarioRepository.findByEmail("f.barra@alumnos.duoc.cl")).thenReturn(Optional.of(usuarioDuoc));
        when(passwordEncoder.matches("mala", "$2a$10$hashSimulado")).thenReturn(false);
        when(usuarioRepository.findByEmail("nadie@gmail.com")).thenReturn(Optional.empty());

        IllegalArgumentException claveMala = assertThrows(IllegalArgumentException.class,
                () -> usuarioService.autenticar("f.barra@alumnos.duoc.cl", "mala"));
        IllegalArgumentException sinCorreo = assertThrows(IllegalArgumentException.class,
                () -> usuarioService.autenticar("nadie@gmail.com", "cualquiera"));

        assertEquals(claveMala.getMessage(), sinCorreo.getMessage());
    }

    @Test
    @DisplayName("El registro público cifra la clave y fuerza el rol CLIENTE aunque se envíe ADMIN")
    void testRegistroCifraClaveYForzaRolCliente() {
        Usuario intruso = new Usuario(
                "15345678-K",
                "Intruso",
                "Prueba",
                "intruso@gmail.com",
                "claveSegura2026",
                "+56911223344",
                LocalDate.of(1995, 3, 20),
                "ADMIN"
        );
        when(usuarioRepository.existsByRut("15345678K")).thenReturn(false);
        when(usuarioRepository.existsByEmail("intruso@gmail.com")).thenReturn(false);
        when(passwordEncoder.encode("claveSegura2026")).thenReturn("$2a$10$cifrado");
        when(usuarioRepository.save(intruso)).thenReturn(intruso);

        Usuario guardado = usuarioService.registrarUsuario(intruso);

        assertEquals("CLIENTE", guardado.getRol());
        assertEquals("$2a$10$cifrado", guardado.getPassword());
    }
}
