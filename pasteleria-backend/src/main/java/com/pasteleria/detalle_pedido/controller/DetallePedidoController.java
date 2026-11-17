package com.pasteleria.detalle_pedido.controller;

import com.pasteleria.detalle_pedido.model.DetallePedido;
import com.pasteleria.detalle_pedido.model.DetallePedidoRequest;
import com.pasteleria.detalle_pedido.service.DetallePedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/detalle-pedidos")
@CrossOrigin(origins = "*")
@Tag(name = "4. Detalle de Pedidos", description = "Endpoints para agregar, ver y quitar productos de un pedido")
public class DetallePedidoController {

    private final DetallePedidoService detallePedidoService;

    public DetallePedidoController(DetallePedidoService detallePedidoService) {
        this.detallePedidoService = detallePedidoService;
    }

    @GetMapping("/pedido/{pedidoId}")
    @Operation(summary = "Ver los productos de un pedido")
    public ResponseEntity<?> obtenerPorPedido(@PathVariable Long pedidoId) {
        try {
            List<DetallePedido> detalles = detallePedidoService.obtenerPorPedido(pedidoId);
            return ResponseEntity.ok(detalles);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Ver una línea de detalle por su ID")
    public ResponseEntity<?> obtenerPorId(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(detallePedidoService.obtenerPorId(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping
    @Operation(summary = "Agregar un producto a un pedido",
            description = "Descuenta stock, guarda el precio del producto y recalcula el total del pedido")
    public ResponseEntity<?> agregarDetalle(@Valid @RequestBody DetallePedidoRequest request) {
        try {
            DetallePedido nuevo = detallePedidoService.agregarDetalle(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Quitar un producto del pedido",
            description = "Devuelve el stock y recalcula el total del pedido")
    public ResponseEntity<?> eliminarDetalle(@PathVariable Long id) {
        try {
            detallePedidoService.eliminarDetalle(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        }
    }
}