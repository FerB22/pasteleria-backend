package com.pasteleria.pedido.controller;

import com.pasteleria.pedido.model.EstadoPedido;
import com.pasteleria.pedido.model.Pedido;
import com.pasteleria.pedido.model.PedidoRequest;
import com.pasteleria.pedido.service.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST del módulo de Pedidos.
 * Expone los endpoints para registrar pedidos, consultarlos y actualizar su estado.
 * La documentación interactiva se genera automáticamente con SpringDoc OpenAPI
 * y queda disponible en Swagger UI (/swagger-ui.html).
 */
@RestController
@RequestMapping("/api/pedidos")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
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
    @Operation(
            summary = "Listar todos los pedidos",
            description = "Devuelve el historial completo de pedidos registrados en el sistema, "
                    + "incluyendo el usuario asociado, el estado, el total y los datos de despacho y pago."
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
                    description = "No existe un pedido con el ID indicado",
                    content = @Content(examples = @ExampleObject(
                            value = "{\"status\": 404, \"error\": \"Not Found\", \"path\": \"/api/pedidos/99\"}"))
            )
    })
    public ResponseEntity<Pedido> obtenerPorId(
            @Parameter(description = "Identificador único del pedido", example = "1", required = true)
            @PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.obtenerPorId(id));
    }

    @GetMapping("/usuario/{usuarioId}")
    @Operation(
            summary = "Listar los pedidos de un usuario",
            description = "Devuelve todos los pedidos realizados por un usuario. "
                    + "Si el usuario no tiene pedidos registrados, retorna una lista vacía."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Pedidos del usuario recuperados correctamente",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = Pedido.class)))
    )
    public ResponseEntity<List<Pedido>> obtenerPorUsuario(
            @Parameter(description = "Identificador del usuario dueño de los pedidos",
                    example = "2", required = true)
            @PathVariable Long usuarioId) {
        return ResponseEntity.ok(pedidoService.obtenerPorUsuario(usuarioId));
    }

    @PostMapping
    @Operation(
            summary = "Registrar un nuevo pedido",
            description = "Crea un pedido en estado PENDIENTE para un usuario existente. "
                    + "El campo total es un valor preliminar: se recalcula automáticamente al agregar o quitar "
                    + "líneas de detalle. El campo descuentoAplicado corresponde a un porcentaje entre 0 y 100."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Pedido creado correctamente",
                    content = @Content(schema = @Schema(implementation = Pedido.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "El usuario indicado no existe",
                    content = @Content(examples = @ExampleObject(
                            value = "{\"status\": 404, \"error\": \"Not Found\", \"path\": \"/api/pedidos\"}"))
            )
    })
    public ResponseEntity<Pedido> crearPedido(
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
            @RequestBody PedidoRequest request) {
        Pedido nuevoPedido = pedidoService.crearPedido(request);
        return new ResponseEntity<>(nuevoPedido, HttpStatus.CREATED);
    }

    @PatchMapping("/{id}/estado")
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
            @ApiResponse(
                    responseCode = "400",
                    description = "El estado enviado no es un valor válido",
                    content = @Content(examples = @ExampleObject(
                            value = "{\"status\": 400, \"error\": \"Bad Request\", \"path\": \"/api/pedidos/1/estado\"}"))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No existe un pedido con el ID indicado",
                    content = @Content(examples = @ExampleObject(
                            value = "{\"status\": 404, \"error\": \"Not Found\", \"path\": \"/api/pedidos/99\"}"))
            )
    })
    public ResponseEntity<Pedido> actualizarEstado(
            @Parameter(description = "Identificador único del pedido", example = "1", required = true)
            @PathVariable Long id,
            @Parameter(description = "Nuevo estado del pedido", required = true, example = "EN_PREPARACION",
                    schema = @Schema(implementation = EstadoPedido.class))
            @RequestParam EstadoPedido estado) {
        return ResponseEntity.ok(pedidoService.actualizarEstado(id, estado));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Eliminar un pedido",
            description = "Elimina un pedido existente. Si el pedido tiene líneas de detalle asociadas, "
                    + "la base de datos puede impedir la operación por integridad referencial."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Pedido eliminado correctamente (sin contenido de respuesta)"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "No existe un pedido con el ID indicado",
                    content = @Content(examples = @ExampleObject(
                            value = "{\"status\": 404, \"error\": \"Not Found\", \"path\": \"/api/pedidos/99\"}"))
            )
    })
    public ResponseEntity<Void> eliminarPedido(
            @Parameter(description = "Identificador único del pedido a eliminar", example = "1", required = true)
            @PathVariable Long id) {
        pedidoService.eliminarPedido(id);
        return ResponseEntity.noContent().build();
    }
}
