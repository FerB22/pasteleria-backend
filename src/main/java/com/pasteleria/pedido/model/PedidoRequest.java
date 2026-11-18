package com.pasteleria.pedido.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Datos de entrada para registrar un nuevo pedido.
 * El total enviado es preliminar: el sistema lo recalcula al agregar o quitar líneas de detalle.
 */
@Schema(description = "Datos de entrada para registrar un nuevo pedido")
public class PedidoRequest {

    @NotNull(message = "El ID del usuario es obligatorio")
    @Schema(description = "ID del usuario que realiza el pedido",
            example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long usuarioId;

    @NotNull(message = "El total es obligatorio")
    @Min(value = 0, message = "El total no puede ser negativo")
    @Schema(description = "Total preliminar del pedido en pesos chilenos (CLP). "
            + "El sistema lo recalcula automáticamente al agregar o quitar detalles",
            example = "0")
    private Integer total;

    @Min(value = 0, message = "El descuento no puede ser negativo")
    @Schema(description = "Porcentaje de descuento a aplicar sobre el pedido (0 a 100)",
            example = "0", minimum = "0", maximum = "100")
    private Integer descuentoAplicado;

    @NotBlank(message = "La dirección de despacho es obligatoria")
    @Schema(description = "Dirección donde se despachará el pedido",
            example = "Av. Libertador Bernardo O'Higgins 1234, Santiago",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String direccionDespacho;

    @NotBlank(message = "El método de pago es obligatorio")
    @Schema(description = "Método de pago seleccionado por el cliente",
            example = "EFECTIVO",
            allowableValues = {"EFECTIVO", "TARJETA", "TRANSFERENCIA"},
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String metodoPago;

    // Getters y Setters
    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public Integer getTotal() { return total; }
    public void setTotal(Integer total) { this.total = total; }

    public Integer getDescuentoAplicado() { return descuentoAplicado; }
    public void setDescuentoAplicado(Integer descuentoAplicado) { this.descuentoAplicado = descuentoAplicado; }

    public String getDireccionDespacho() { return direccionDespacho; }
    public void setDireccionDespacho(String direccionDespacho) { this.direccionDespacho = direccionDespacho; }

    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }
}