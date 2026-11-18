package com.pasteleria.pedido.controller;

import com.pasteleria.pedido.model.EstadoPedido;
import com.pasteleria.pedido.model.Pedido;
import com.pasteleria.pedido.model.PedidoRequest;
import com.pasteleria.pedido.service.PedidoService;
import com.pasteleria.usuario.security.UsuarioAutenticado;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/v1/pedidos", "/api/pedidos"})
@CrossOrigin(origins = "*")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(
        name = "3. Gestión de Pedidos",
        description = "Endpoints para registrar pedidos, consultar el historial por usuario "
                + "y administrar el ciclo de vida de cada pedido"
)
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    @Operation(
            summary = "Listar todos los pedidos",
            description = "Solo disponible para ADMIN o VENDEDOR. Devuelve el historial completo de pedidos registrados en el sistema."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Historial de pedidos obtenido correctamente",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Pedido.class)))
    )
    public ResponseEntity<List<Pedido>> obtenerTodos() {
        return ResponseEntity.ok(pedidoService.obtenerTodos());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Buscar un pedido por su ID",
            description = "Recupera la información de un pedido a partir de su identificador primario."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Pedido encontrado",
                    content = @Content(schema = @Schema(implementation = Pedido.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No existe un pedido con el ID indicado"
            )
    })
    public ResponseEntity<?> obtenerPorId(
            @Parameter(description = "Identificador único del pedido", example = "1", required = true)
            @PathVariable Long id,
            @AuthenticationPrincipal UsuarioAutenticado principal) {
        try {
            Pedido pedido = pedidoService.obtenerPorId(id);
            if (!esStaffOPropietario(principal, pedido.getUsuario().getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Acceso denegado: no puedes consultar pedidos de otro usuario"));
            }
            return ResponseEntity.ok(pedido);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/usuario/{usuarioId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR') or #usuarioId == principal.id")
    @Operation(
            summary = "Listar los pedidos de un usuario",
            description = "Devuelve todos los pedidos realizados por un usuario. Si no tiene pedidos, retorna una lista vacía."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Pedidos del usuario recuperados correctamente",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Pedido.class)))
    )
    public ResponseEntity<List<Pedido>> obtenerPorUsuario(
            @Parameter(description = "Identificador del usuario dueño de los pedidos", example = "2", required = true)
            @PathVariable Long usuarioId) {
        return ResponseEntity.ok(pedidoService.obtenerPorUsuario(usuarioId));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(
            summary = "Registrar un nuevo pedido",
            description = "Crea un pedido en estado PENDIENTE para un usuario existente. "
                    + "El campo total se recalcula automáticamente al agregar o quitar líneas de detalle."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Pedido creado correctamente",
                    content = @Content(schema = @Schema(implementation = Pedido.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "El usuario indicado no existe"
            )
    })
    public ResponseEntity<?> crearPedido(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos necesarios para registrar el pedido",
                    required = true,
                    content = @Content(examples = @ExampleObject(value = "{\n"
                            + "  \"usuarioId\": 2,\n"
                            + "  \"total\": 0,\n"
                            + "  \"descuentoAplicado\": 0,\n"
                            + "  \"direccionDespacho\": \"Av. Libertador Bernardo O'Higgins 1234, Santiago\",\n"
                            + "  \"metodoPago\": \"EFECTIVO\"\n"
                            + "}"))
            )
            @Valid @RequestBody PedidoRequest request,
            @AuthenticationPrincipal UsuarioAutenticado principal) {
        if (principal != null
                && !esStaff(principal)
                && !principal.getId().equals(request.getUsuarioId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Acceso denegado: no puedes crear pedidos para otro usuario"));
        }
        try {
            Pedido nuevoPedido = pedidoService.crearPedido(request);
            return new ResponseEntity<>(nuevoPedido, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    @Operation(
            summary = "Actualizar el estado de un pedido",
            description = "Cambia el estado del pedido dentro del flujo: "
                    + "PENDIENTE -> EN_PREPARACION -> LISTO_PARA_ENTREGA -> ENTREGADO, o bien CANCELADO."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Estado del pedido actualizado correctamente",
                    content = @Content(schema = @Schema(implementation = Pedido.class))
            ),
            @ApiResponse(responseCode = "400", description = "El estado enviado no es un valor válido"),
            @ApiResponse(responseCode = "404", description = "No existe un pedido con el ID indicado")
    })
    public ResponseEntity<?> actualizarEstado(
            @Parameter(description = "Identificador único del pedido", example = "1", required = true)
            @PathVariable Long id,
            @Parameter(description = "Nuevo estado del pedido", required = true, example = "EN_PREPARACION",
                    schema = @Schema(implementation = EstadoPedido.class))
            @RequestParam EstadoPedido estado) {
        try {
            return ResponseEntity.ok(pedidoService.actualizarEstado(id, estado));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Eliminar un pedido por ID",
            description = "Elimina un pedido existente (solo ADMIN)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Pedido eliminado correctamente"),
            @ApiResponse(responseCode = "404", description = "No existe un pedido con el ID indicado")
    })
    public ResponseEntity<?> eliminarPedido(
            @Parameter(description = "Identificador único del pedido a eliminar", example = "1", required = true)
            @PathVariable Long id) {
        try {
            pedidoService.eliminarPedido(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> manejarValidaciones(MethodArgumentNotValidException ex) {
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getDefaultMessage())
                .filter(msg -> msg != null && !msg.isBlank())
                .findFirst()
                .orElse("Datos de pedido inválidos");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", mensaje));
    }

    private static boolean esStaff(UsuarioAutenticado principal) {
        if (principal == null || principal.getRol() == null) {
            return false;
        }
        return "ADMIN".equalsIgnoreCase(principal.getRol())
                || "VENDEDOR".equalsIgnoreCase(principal.getRol());
    }

    private static boolean esStaffOPropietario(UsuarioAutenticado principal, Long propietarioId) {
        if (principal == null) {
            return false;
        }
        return esStaff(principal) || (propietarioId != null && propietarioId.equals(principal.getId()));
    }
}
