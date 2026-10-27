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

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuarioSenior;
    private Usuario usuarioDuoc;

    @BeforeEach
    void setUp() {
        usuarioSenior = new Usuario(
                "15345678-5",
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
                "20123456-7",
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
        assertTrue(usuarioService.validarRutChileno("15.345.678-5"));
        assertTrue(usuarioService.validarRutChileno("20123456-7"));
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
}
