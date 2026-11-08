package cl.duoc.pasteleria_backend.DTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PedidoRequestDTO {

    @NotBlank(message = "El ID del cliente es obligatorio")
    private String clienteId;

    @NotBlank(message = "La dirección de entrega es obligatoria")
    private String direccionEntrega;

    @NotBlank(message = "El método de pago es obligatorio")
    private String metodoPago;

    @NotEmpty(message = "El pedido debe contener al menos un producto")
    @Valid
    private List<ItemRequestDTO> items;

    @Data
    public static class ItemRequestDTO {

        @NotNull(message = "El ID del producto es obligatorio")
        private Long productoId;

        @NotNull(message = "La cantidad es obligatoria")
        @Positive(message = "La cantidad debe ser mayor a 0")
        private Integer cantidad;

        @NotNull(message = "El precio unitario es obligatorio")
        @Positive(message = "El precio unitario debe ser mayor a 0")
        private BigDecimal precioUnitario;
    }
}