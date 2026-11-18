package com.pasteleria.detalle_pedido.controller;

import com.pasteleria.detalle_pedido.model.DetallePedido;
import com.pasteleria.detalle_pedido.model.DetallePedidoRequest;
import com.pasteleria.detalle_pedido.service.DetallePedidoService;
import com.pasteleria.pedido.model.Pedido;
import com.pasteleria.pedido.service.PedidoService;
import com.pasteleria.usuario.security.UsuarioAutenticado;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping({"/api/v1/detalle-pedidos", "/api/detalle-pedidos"})
@CrossOrigin(origins = "*")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "4. Detalle de Pedidos", description = "Endpoints para agregar, ver y quitar productos de un pedido")
public class DetallePedidoController {

    private final DetallePedidoService detallePedidoService;
    private final PedidoService pedidoService;

    public DetallePedidoController(DetallePedidoService detallePedidoService, PedidoService pedidoService) {
        this.detallePedidoService = detallePedidoService;
        this.pedidoService = pedidoService;
    }

    @GetMapping("/pedido/{pedidoId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Ver los productos de un pedido")
    public ResponseEntity<?> obtenerPorPedido(
            @PathVariable Long pedidoId,
            @AuthenticationPrincipal UsuarioAutenticado principal) {
        try {
            Pedido pedido = pedidoService.obtenerPorId(pedidoId);
            if (!esStaffOPropietario(principal, pedido.getUsuario().getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Acceso denegado: no puedes ver detalles de un pedido ajeno"));
            }
            List<DetallePedido> detalles = detallePedidoService.obtenerPorPedido(pedidoId);
            return ResponseEntity.ok(detalles);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Ver una línea de detalle por su ID")
    public ResponseEntity<?> obtenerPorId(
            @PathVariable Long id,
            @AuthenticationPrincipal UsuarioAutenticado principal) {
        try {
            DetallePedido detalle = detallePedidoService.obtenerPorId(id);
            if (!esStaffOPropietario(principal, detalle.getPedido().getUsuario().getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Acceso denegado: no puedes consultar un detalle ajeno"));
            }
            return ResponseEntity.ok(detalle);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Agregar un producto a un pedido",
            description = "Descuenta stock, guarda el precio del producto y recalcula el total del pedido")
    public ResponseEntity<?> agregarDetalle(
            @Valid @RequestBody DetallePedidoRequest request,
            @AuthenticationPrincipal UsuarioAutenticado principal) {
        try {
            Pedido pedido = pedidoService.obtenerPorId(request.getPedidoId());
            if (!esStaffOPropietario(principal, pedido.getUsuario().getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Acceso denegado: no puedes modificar un pedido ajeno"));
            }
            DetallePedido nuevo = detallePedidoService.agregarDetalle(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/lote")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Agregar todos los productos del carrito a un pedido",
            description = "Si un producto falla, no se guarda ninguno y el stock queda como estaba")
    public ResponseEntity<?> agregarVarios(
            @RequestBody List<@Valid DetallePedidoRequest> requests,
            @AuthenticationPrincipal UsuarioAutenticado principal) {
        if (requests == null || requests.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "El carrito no tiene productos"));
        }
        try {
            for (DetallePedidoRequest req : requests) {
                Pedido pedido = pedidoService.obtenerPorId(req.getPedidoId());
                if (!esStaffOPropietario(principal, pedido.getUsuario().getId())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(Map.of("error", "Acceso denegado: no puedes modificar un pedido ajeno"));
                }
            }
            List<DetallePedido> nuevos = detallePedidoService.agregarVarios(requests);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevos);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Quitar un producto del pedido",
            description = "Devuelve el stock y recalcula el total del pedido")
    public ResponseEntity<?> eliminarDetalle(
            @PathVariable Long id,
            @AuthenticationPrincipal UsuarioAutenticado principal) {
        try {
            DetallePedido detalle = detallePedidoService.obtenerPorId(id);
            Long propietarioId = detalle.getPedido().getUsuario().getId();
            boolean esAdmin = principal != null && "ADMIN".equalsIgnoreCase(principal.getRol());
            boolean esPropietario = principal != null && principal.getId().equals(propietarioId);
            if (!esAdmin && !esPropietario) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Acceso denegado: solo ADMIN o el dueño pueden quitar productos"));
            }
            detallePedidoService.eliminarDetalle(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> manejarValidaciones(MethodArgumentNotValidException ex) {
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getDefaultMessage())
                .filter(msg -> msg != null && !msg.isBlank())
                .findFirst()
                .orElse("Datos de detalle inválidos");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", mensaje));
    }

    private static boolean esStaffOPropietario(UsuarioAutenticado principal, Long propietarioId) {
        if (principal == null) {
            return false;
        }
        String rol = principal.getRol();
        boolean esStaff = "ADMIN".equalsIgnoreCase(rol) || "VENDEDOR".equalsIgnoreCase(rol);
        return esStaff || (propietarioId != null && propietarioId.equals(principal.getId()));
    }
}
