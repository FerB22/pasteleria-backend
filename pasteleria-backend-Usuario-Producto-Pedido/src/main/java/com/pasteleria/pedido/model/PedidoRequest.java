package com.pasteleria.pedido.model;

public class PedidoRequest {

    private Long usuarioId;
    private Integer total;
    private Integer descuentoAplicado;
    private String direccionDespacho;
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