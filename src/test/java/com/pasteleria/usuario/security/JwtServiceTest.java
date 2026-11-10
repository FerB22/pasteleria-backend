package com.pasteleria.usuario.security;

import com.pasteleria.usuario.model.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas unitarias de JwtService: generación, verificación, manipulación y expiración.
 */
class JwtServiceTest {

    private static final String SECRETO = "clave-de-prueba-con-mas-de-treinta-y-dos-caracteres";

    private Usuario usuarioAdmin() {
        Usuario u = new Usuario("15345678-K", "Alonso", "Contreras", "admin@milsabores.cl",
                "hash", "+56911223344", LocalDate.of(1995, 3, 15), "ADMIN");
        u.setId(7L);
        return u;
    }

    @Test
    @DisplayName("Un token recién generado se valida y conserva id, correo y rol")
    void tokenValidoConservaClaims() {
        JwtService jwt = new JwtService(SECRETO, 60_000);

        var resultado = jwt.validar(jwt.generarToken(usuarioAdmin()));

        assertTrue(resultado.isPresent());
        assertEquals(7L, resultado.get().getId());
        assertEquals("admin@milsabores.cl", resultado.get().getEmail());
        assertEquals("ADMIN", resultado.get().getRol());
    }

    @Test
    @DisplayName("Un token alterado (firma modificada) es rechazado")
    void tokenAlteradoEsRechazado() {
        JwtService jwt = new JwtService(SECRETO, 60_000);
        String token = jwt.generarToken(usuarioAdmin());

        String[] partes = token.split("\\.");
        char original = partes[1].charAt(5);
        char distinto = original == 'A' ? 'B' : 'A';
        partes[1] = partes[1].substring(0, 5) + distinto + partes[1].substring(6);
        assertTrue(jwt.validar(String.join(".", partes)).isEmpty());
    }

    @Test
    @DisplayName("Un token firmado con otra clave es rechazado")
    void tokenDeOtraClaveEsRechazado() {
        JwtService emisor = new JwtService("otra-clave-distinta-con-mas-de-32-caracteres!!", 60_000);
        JwtService verificador = new JwtService(SECRETO, 60_000);

        assertTrue(verificador.validar(emisor.generarToken(usuarioAdmin())).isEmpty());
    }

    @Test
    @DisplayName("Un token expirado es rechazado")
    void tokenExpiradoEsRechazado() {
        JwtService jwt = new JwtService(SECRETO, -1000);

        assertTrue(jwt.validar(jwt.generarToken(usuarioAdmin())).isEmpty());
    }

    @Test
    @DisplayName("Texto que no es un JWT es rechazado sin lanzar excepción")
    void textoBasuraEsRechazado() {
        JwtService jwt = new JwtService(SECRETO, 60_000);

        assertTrue(jwt.validar("esto-no-es-un-jwt").isEmpty());
    }

    @Test
    @DisplayName("Una clave menor a 32 bytes no se acepta al configurar el servicio")
    void claveCortaNoSeAcepta() {
        assertThrows(IllegalStateException.class, () -> new JwtService("corta", 60_000));
    }
}
