package com.pasteleria.usuario.controller;

import com.pasteleria.usuario.model.Usuario;
import com.pasteleria.usuario.security.JwtService;
import com.pasteleria.usuario.security.UsuarioAutenticado;
import com.pasteleria.usuario.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador RESTful que expone la API pública y privada para el módulo de Usuarios.
 * Admite peticiones desde el frontend React mediante @CrossOrigin.
 * Incorpora versionado en la ruta (/api/v1/usuarios) conforme a las buenas prácticas exigidas.
 */
@RestController
@RequestMapping({"/api/v1/usuarios", "/api/usuarios"})
@CrossOrigin(origins = "*")
@Tag(name = "1. Gestión de Usuarios y Autenticación", description = "Endpoints para registro, login, cálculo de beneficios comerciales del 50 aniversario y operaciones CRUD")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final JwtService jwtService;

    public UsuarioController(UsuarioService usuarioService, JwtService jwtService) {
        this.usuarioService = usuarioService;
        this.jwtService = jwtService;
    }

    /** Arma la respuesta de autenticación: token JWT, vigencia y datos públicos del usuario. */
    private Map<String, Object> respuestaConToken(Usuario usuario) {
        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("token", jwtService.generarToken(usuario));
        respuesta.put("tipo", "Bearer");
        respuesta.put("expiraEnMs", jwtService.getExpiracionMs());
        respuesta.put("usuario", usuario);
        return respuesta;
    }

    @PostMapping("/registro")
    @Operation(
            summary = "Registrar nuevo usuario",
            description = "Crea un usuario en la base de datos previa validación estricta del RUT chileno mediante algoritmo Módulo 11 (descartando secuencias repetitivas) y verificación de formato de correo electrónico."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario registrado exitosamente en la base de datos relacional",
                    content = @Content(schema = @Schema(implementation = Usuario.class))),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos (RUT erróneo, correo inválido o campos obligatorios en blanco)",
                    content = @Content(examples = @ExampleObject(value = "{\"error\": \"El RUT ingresado (12.345.678-0) es inválido según el algoritmo Módulo 11\"}"))),
            @ApiResponse(responseCode = "409", description = "Conflicto: El RUT o correo ya se encuentra registrado")
    })
    public ResponseEntity<?> registrar(@Valid @RequestBody Usuario usuario) {
        try {
            Usuario nuevo = usuarioService.registrarUsuario(usuario);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (IllegalStateException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }
    }

    @PostMapping("/login")
    @Operation(
            summary = "Autenticar credenciales de usuario",
            description = "Valida el correo y contraseña (hash BCrypt) contra la base de datos. Retorna un token JWT firmado (válido 1 hora) junto con los datos del usuario y su rol (ADMIN, VENDEDOR o CLIENTE). Use el token en el botón Authorize de Swagger."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Autenticación satisfactoria: retorna token JWT y usuario",
                    content = @Content(examples = @ExampleObject(value = "{\"token\": \"eyJhbGciOiJIUzI1NiJ9...\", \"tipo\": \"Bearer\", \"expiraEnMs\": 3600000, \"usuario\": {\"id\": 1, \"nombre\": \"Alonso\", \"rol\": \"ADMIN\"}}"))),
            @ApiResponse(responseCode = "401", description = "Credenciales incorrectas",
                    content = @Content(examples = @ExampleObject(value = "{\"error\": \"Credenciales inválidas\"}"))),
            @ApiResponse(responseCode = "400", description = "Parámetros de login incompletos o en blanco")
    })
    public ResponseEntity<?> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Credenciales de acceso",
                    required = true,
                    content = @Content(examples = @ExampleObject(value = "{\"email\": \"f.barra@alumnos.duoc.cl\", \"password\": \"duoc2026\"}"))
            )
            @RequestBody Map<String, String> credenciales) {
        try {
            String email = credenciales.get("email");
            String password = credenciales.get("password");
            Usuario usuario = usuarioService.autenticar(email, password);
            return ResponseEntity.ok(respuestaConToken(usuario));
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    @PostMapping("/refresh")
    @Operation(
            summary = "Renovar token JWT",
            description = "Recibe un token todavía vigente (cabecera Authorization) y emite uno nuevo con el rol actual del usuario en base de datos."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token renovado"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido o expirado")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<?> renovarToken(Authentication authentication) {
        UsuarioAutenticado actual = (UsuarioAutenticado) authentication.getPrincipal();
        try {
            Usuario usuario = usuarioService.obtenerPorId(actual.getId());
            return ResponseEntity.ok(respuestaConToken(usuario));
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "El usuario del token ya no existe");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
    }

    @GetMapping
    @Operation(
            summary = "Listar catálogo completo de usuarios",
            description = "Obtiene la lista de todos los usuarios registrados en el sistema. Destinado a la administración de usuarios."
    )
    @ApiResponse(responseCode = "200", description = "Lista completa de usuarios recuperada exitosamente")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<List<Usuario>> listarTodos() {
        return ResponseEntity.ok(usuarioService.obtenerTodos());
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar usuario por ID",
            description = "Recupera la información detallada de un usuario a partir de su identificador primario."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario encontrado",
                    content = @Content(schema = @Schema(implementation = Usuario.class))),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado para el ID suministrado")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ADMIN') or #id == principal.id")
    public ResponseEntity<?> buscarPorId(
            @Parameter(description = "Identificador único del usuario", example = "1", required = true)
            @PathVariable Long id) {
        try {
            return ResponseEntity.ok(usuarioService.obtenerPorId(id));
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @GetMapping("/{id}/beneficios")
    @Operation(
            summary = "Consultar beneficios y descuentos comerciales (50 Aniversario)",
            description = "Calcula dinámicamente en tiempo de ejecución los beneficios del usuario: 50 % de descuento si edad >= 50 años (calculada desde fechaNacimiento), 10 % si aplica el cupón 'FELICES50' y torta de cumpleaños gratis para estudiantes Duoc UC (@duocuc.cl o @alumnos.duoc.cl)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cálculo de beneficios retornado con éxito",
                    content = @Content(examples = @ExampleObject(value = "{\n  \"usuarioId\": 2,\n  \"nombreCompleto\": \"Marta González\",\n  \"edadCalculada\": 58,\n  \"descuentoPorcentaje\": 50,\n  \"motivoDescuento\": \"Beneficio 50 aniversario: Cliente con 50 años o más\",\n  \"beneficioDuocCumpleanos\": \"No aplica convenio Duoc UC\"\n}"))),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ADMIN') or #id == principal.id")
    public ResponseEntity<?> consultarBeneficios(
            @Parameter(description = "Identificador único del usuario", example = "2", required = true)
            @PathVariable Long id,
            @Parameter(description = "Código de cupón promocional opcional (ej: FELICES50)", example = "FELICES50")
            @RequestParam(required = false) String cupon) {
        try {
            Map<String, Object> beneficios = usuarioService.calcularBeneficios(id, cupon);
            return ResponseEntity.ok(beneficios);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Actualizar datos personales de usuario",
            description = "Permite modificar nombre, apellido, teléfono y fecha de nacimiento de un usuario existente."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario actualizado exitosamente",
                    content = @Content(schema = @Schema(implementation = Usuario.class))),
            @ApiResponse(responseCode = "400", description = "Datos de actualización inválidos"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('ADMIN') or #id == principal.id")
    public ResponseEntity<?> actualizar(
            @Parameter(description = "Identificador único del usuario a actualizar", example = "1", required = true)
            @PathVariable Long id,
            @RequestBody Usuario datos) {
        try {
            Usuario actualizado = usuarioService.actualizarUsuario(id, datos);
            return ResponseEntity.ok(actualizado);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Eliminar usuario del sistema",
            description = "Suprime el registro de un usuario en base a su ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Usuario eliminado exitosamente"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado para eliminar")
    })
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<?> eliminar(
            @Parameter(description = "Identificador único del usuario a eliminar", example = "3", required = true)
            @PathVariable Long id) {
        try {
            usuarioService.eliminarUsuario(id);
            Map<String, String> respuesta = new HashMap<>();
            respuesta.put("mensaje", "Usuario eliminado exitosamente con ID: " + id);
            return ResponseEntity.ok(respuesta);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
    }
}
