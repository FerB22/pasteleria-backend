package com.pasteleria.pedido.model;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Ciclo de vida de un pedido dentro de la plataforma.
 * El flujo normal es: PENDIENTE -> EN_PREPARACION -> LISTO_PARA_ENTREGA -> ENTREGADO.
 * Un pedido puede pasar a CANCELADO mientras no haya sido entregado.
 */
@Schema(description = "Estados posibles del ciclo de vida de un pedido")
public enum EstadoPedido {

    @Schema(description = "Pedido registrado y aún no iniciado")
    PENDIENTE,

    @Schema(description = "Pedido en preparación en cocina")
    EN_PREPARACION,

    @Schema(description = "Pedido listo para ser retirado o despachado")
    LISTO_PARA_ENTREGA,

    @Schema(description = "Pedido entregado al cliente")
    ENTREGADO,

    @Schema(description = "Pedido cancelado")
    CANCELADO
}
