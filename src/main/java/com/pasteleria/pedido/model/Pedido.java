package com.pasteleria.pedido.model;

import com.pasteleria.usuario.model.Usuario;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad JPA que representa un pedido realizado por un usuario.
 * El total se recalcula a partir de las líneas de detalle del pedido.
 */
@Entity
@Table(name = "pedidos")
@Schema(description = "Pedido realizado por un usuario en la plataforma de Pastelería Mil Sabores")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único autogenerado del pedido",
            example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    @Schema(description = "Usuario que realizó el pedido", accessMode = Schema.AccessMode.READ_ONLY)
    private Usuario usuario;

    @Column(name = "fecha_pedido", nullable = false)
    @Schema(description = "Fecha y hora en que se registró el pedido",
            example = "2026-11-16T10:30:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime fechaPedido;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Schema(description = "Estado actual del pedido", example = "PENDIENTE")
    private EstadoPedido estado;

    @Column(nullable = false)
    @Schema(description = "Monto total del pedido en pesos chilenos (CLP). "
            + "Se recalcula al agregar o quitar líneas de detalle",
            example = "22500")
    private Integer total;

    @Column(name = "descuento_aplicado")
    @Schema(description = "Porcentaje de descuento aplicado al pedido (0 a 100)",
            example = "0", minimum = "0", maximum = "100")
    private Integer descuentoAplicado;

    @Column(name = "direccion_despacho", nullable = false, length = 255)
    @Schema(description = "Dirección donde se despachará el pedido",
            example = "Av. Libertador Bernardo O'Higgins 1234, Santiago")
    private String direccionDespacho;

    @Column(name = "metodo_pago", nullable = false, length = 50)
    @Schema(description = "Método de pago seleccionado por el cliente",
            example = "EFECTIVO",
            allowableValues = {"EFECTIVO", "TARJETA", "TRANSFERENCIA"})
    private String metodoPago;

    public Pedido() {
        this.fechaPedido = LocalDateTime.now();
        this.estado = EstadoPedido.PENDIENTE;
        this.descuentoAplicado = 0;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public LocalDateTime getFechaPedido() { return fechaPedido; }
    public void setFechaPedido(LocalDateTime fechaPedido) { this.fechaPedido = fechaPedido; }

    public EstadoPedido getEstado() { return estado; }
    public void setEstado(EstadoPedido estado) { this.estado = estado; }

    public Integer getTotal() { return total; }
    public void setTotal(Integer total) { this.total = total; }

    public Integer getDescuentoAplicado() { return descuentoAplicado; }
    public void setDescuentoAplicado(Integer descuentoAplicado) { this.descuentoAplicado = descuentoAplicado; }

    public String getDireccionDespacho() { return direccionDespacho; }
    public void setDireccionDespacho(String direccionDespacho) { this.direccionDespacho = direccionDespacho; }

    public String getMetodoPago() { return metodoPago; }
    public void setMetodoPago(String metodoPago) { this.metodoPago = metodoPago; }
}