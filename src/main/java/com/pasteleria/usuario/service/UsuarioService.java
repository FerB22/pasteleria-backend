package com.pasteleria.usuario.service;

import com.pasteleria.usuario.model.Usuario;
import com.pasteleria.usuario.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Capa de servicio de negocio para la gestión de usuarios y beneficios comerciales.
 * Implementa las validaciones de negocio chilenas (RUT con Módulo 11 y correo)
 * y los descuentos del 50 aniversario de Pastelería Mil Sabores.
 */
@Service
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    // Expresión regular de máxima restricción:
    // 1. Usuario local: mínimo 3 caracteres alfanuméricos (rechaza 'f')
    // 2. Segmentos de dominio: mínimo 2 caracteres por etiqueta (rechaza 'j.j')
    // 3. Prohíbe puntos consecutivos ('..')
    private static final Pattern EMAIL_STRICT_PATTERN = Pattern.compile(
            "^(?!.*\\.\\.)[a-zA-Z0-9][a-zA-Z0-9._%+-]{2,63}@[a-zA-Z0-9](?:[a-zA-Z0-9-]{1,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z]{2,6})+$"
    );

    // Lista blanca opcional de dominios autorizados (política de máxima restricción)
    private static final java.util.Set<String> DOMINIOS_AUTORIZADOS = java.util.Set.of(
            "gmail.com",
            "outlook.com",
            "hotmail.com",
            "yahoo.com",
            "icloud.com",
            "duocuc.cl",
            "alumnos.duoc.cl",
            "profesor.duoc.cl"
    );

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Valida un RUT chileno aplicando el algoritmo matemático del Módulo 11.
     * Prioriza el uso de .isBlank() de Java 21 para prevenir instanciaciones innecesarias.
     */
    public boolean validarRutChileno(String rut) {
        if (rut == null || rut.isBlank()) {
            return false;
        }

        // Limpieza de caracteres no numéricos excepto 'K'
        String limpio = rut.replace(".", "").replace("-", "").trim().toUpperCase();

        // Un RUT válido tiene entre 8 y 9 caracteres (7 u 8 dígitos + 1 verificador)
        if (limpio.length() < 8 || limpio.length() > 9) {
            return false;
        }

        String cuerpo = limpio.substring(0, limpio.length() - 1);
        char dvIngresado = limpio.charAt(limpio.length() - 1);

        // Descartar secuencias repetitivas inválidas comunes (ej: 11111111, 22222222)
        if (cuerpo.chars().distinct().count() == 1) {
            return false;
        }

        // El cuerpo debe contener únicamente dígitos numéricos
        if (!cuerpo.chars().allMatch(Character::isDigit)) {
            return false;
        }

        // Algoritmo Módulo 11 con serie ponderada decreciente de 2 a 7
        int suma = 0;
        int multiplicador = 2;

        for (int i = cuerpo.length() - 1; i >= 0; i--) {
            int digito = Character.getNumericValue(cuerpo.charAt(i));
            suma += digito * multiplicador;
            multiplicador = (multiplicador == 7) ? 2 : multiplicador + 1;
        }

        int resto = 11 - (suma % 11);
        char dvEsperado;

        if (resto == 11) {
            dvEsperado = '0';
        } else if (resto == 10) {
            dvEsperado = 'K';
        } else {
            dvEsperado = Character.forDigit(resto, 10);
        }

        return Character.toUpperCase(dvIngresado) == dvEsperado;
    }

    /**
     * Valida el correo electrónico aplicando el principio de máxima restricción (Default Deny).
     * Rechaza correos sintéticos como 'f@j.j.cl', exigiendo un usuario de al menos 3 caracteres,
     * segmentos de dominio de mínimo 2 caracteres y extensión legítima.
     */
    public boolean validarEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }

        String limpio = email.trim().toLowerCase();

        // 1. Validación sintáctica de alta restricción
        if (!EMAIL_STRICT_PATTERN.matcher(limpio).matches()) {
            return false;
        }

        // 2. Extraer el dominio y verificar que cada segmento tenga al menos 2 caracteres
        int posArroba = limpio.indexOf('@');
        String dominio = limpio.substring(posArroba + 1);
        String[] partesDominio = dominio.split("\\.");

        for (String parte : partesDominio) {
            if (parte.length() < 2) {
                return false; // Rechaza segmentos de 1 letra como 'j' en 'j.j.cl'
            }
        }

        return true;
    }

    /**
     * Registra un nuevo usuario aplicando todas las validaciones de negocio del sistema.
     */
    public Usuario registrarUsuario(Usuario usuario) {
        if (usuario == null) {
            throw new IllegalArgumentException("Los datos del usuario no pueden ser nulos");
        }

        // Validación estricta de RUT chileno
        if (!validarRutChileno(usuario.getRut())) {
            throw new IllegalArgumentException("El RUT ingresado (" + usuario.getRut() + ") es inválido según el algoritmo Módulo 11");
        }

        // Validación de correo electrónico
        if (!validarEmail(usuario.getEmail())) {
            throw new IllegalArgumentException("El correo electrónico ingresado no posee un formato válido");
        }

        // Verificación de unicidad en base de datos
        String rutLimpio = usuario.getRut().replace(".", "").replace("-", "").trim().toUpperCase();
        if (usuarioRepository.existsByRut(rutLimpio)) {
            throw new IllegalStateException("El RUT ya se encuentra registrado en el sistema");
        }

        String emailLimpio = usuario.getEmail().trim().toLowerCase();
        if (usuarioRepository.existsByEmail(emailLimpio)) {
            throw new IllegalStateException("El correo electrónico ya se encuentra registrado en el sistema");
        }

        // Normalización antes de persistir
        usuario.setRut(rutLimpio);
        usuario.setEmail(emailLimpio);

        return usuarioRepository.save(usuario);
    }

    /**
     * Autenticación básica de usuario por correo y contraseña.
     */
    @Transactional(readOnly = true)
    public Usuario autenticar(String email, String password) {
        if (email == null || email.isBlank() || password == null || password.isBlank()) {
            throw new IllegalArgumentException("El correo y la contraseña son obligatorios");
        }

        String emailLimpio = email.trim().toLowerCase();
        Usuario usuario = usuarioRepository.findByEmail(emailLimpio)
                .orElseThrow(() -> new IllegalArgumentException("Credenciales inválidas: usuario no encontrado"));

        if (!usuario.getPassword().equals(password)) {
            throw new IllegalArgumentException("Credenciales inválidas: contraseña incorrecta");
        }

        return usuario;
    }

    /**
     * Calcula los beneficios comerciales aplicables a un usuario y compra.
     * Aplica la regla del 50 aniversario (50 % descuento para >= 50 años), cupón FELICES50 y convenio Duoc UC.
     */
    @Transactional(readOnly = true)
    public Map<String, Object> calcularBeneficios(Long usuarioId, String cuponOpcional) {
        Usuario usuario = obtenerPorId(usuarioId);
        int edad = usuario.calcularEdad();
        boolean calificaCincuentaAnos = usuario.calificaParaDescuentoCincuentaAnos();
        boolean esDuoc = usuario.esUsuarioDuoc();

        int porcentajeDescuento = 0;
        String motivoDescuento = "Sin descuento aplicable";

        // Regla 1: 50 % de descuento para clientes con 50 años o más
        if (calificaCincuentaAnos) {
            porcentajeDescuento = 50;
            motivoDescuento = "Beneficio 50 aniversario: Cliente con 50 años o más";
        } else if (cuponOpcional != null && !cuponOpcional.isBlank() && cuponOpcional.trim().equalsIgnoreCase("FELICES50")) {
            // Regla 2: 10 % de descuento por cupón promocional FELICES50
            porcentajeDescuento = 10;
            motivoDescuento = "Cupón promocional FELICES50 aplicado con éxito";
        }

        Map<String, Object> resumen = new HashMap<>();
        resumen.put("usuarioId", usuario.getId());
        resumen.put("nombreCompleto", usuario.getNombre() + " " + usuario.getApellido());
        resumen.put("edadCalculada", edad);
        resumen.put("descuentoPorcentaje", porcentajeDescuento);
        resumen.put("motivoDescuento", motivoDescuento);
        resumen.put("beneficioDuocCumpleanos", esDuoc ? "Torta gratis de cumpleaños aplicable en el mes natal" : "No aplica convenio Duoc UC");

        return resumen;
    }

    // Operaciones CRUD de administración

    @Transactional(readOnly = true)
    public List<Usuario> obtenerTodos() {
        return usuarioRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Usuario obtenerPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el usuario con ID: " + id));
    }

    public Usuario actualizarUsuario(Long id, Usuario datosActualizados) {
        Usuario existente = obtenerPorId(id);

        if (datosActualizados.getNombre() != null && !datosActualizados.getNombre().isBlank()) {
            existente.setNombre(datosActualizados.getNombre().trim());
        }
        if (datosActualizados.getApellido() != null && !datosActualizados.getApellido().isBlank()) {
            existente.setApellido(datosActualizados.getApellido().trim());
        }
        if (datosActualizados.getTelefono() != null) {
            existente.setTelefono(datosActualizados.getTelefono().trim());
        }
        if (datosActualizados.getFechaNacimiento() != null) {
            existente.setFechaNacimiento(datosActualizados.getFechaNacimiento());
        }

        return usuarioRepository.save(existente);
    }

    public void eliminarUsuario(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new IllegalArgumentException("No es posible eliminar: el usuario no existe con ID: " + id);
        }
        usuarioRepository.deleteById(id);
    }
}
