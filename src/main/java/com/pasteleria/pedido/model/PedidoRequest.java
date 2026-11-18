package com.pasteleria.pedido.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Datos de entrada para registrar un nuevo pedido.
 * El total enviado es preliminar: el sistema lo recalcula al agregar o quitar líneas de detalle.
 */
@Schema(description = "Datos de entrada para registrar un nuevo pedido")
public class PedidoRequest {

    @Schema(description = "ID del usuario que realiza el pedido",
            example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long usuarioId;

    @Schema(description = "Total preliminar del pedido en pesos chilenos (CLP). "
            + "El sistema lo recalcula automáticamente al agregar o quitar detalles",
            example = "0")
    private Integer total;

    @Schema(description = "Porcentaje de descuento a aplicar sobre el pedido (0 a 100)",
            example = "0", minimum = "0", maximum = "100")
    private Integer descuentoAplicado;

    @Schema(description = "Dirección donde se despachará el pedido",
            example = "Av. Libertador Bernardo O'Higgins 1234, Santiago",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String direccionDespacho;

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
