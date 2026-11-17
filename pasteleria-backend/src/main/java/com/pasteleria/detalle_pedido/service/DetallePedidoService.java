package com.pasteleria.detalle_pedido.service;

import com.pasteleria.detalle_pedido.model.DetallePedido;
import com.pasteleria.detalle_pedido.model.DetallePedidoRequest;
import com.pasteleria.detalle_pedido.repository.DetallePedidoRepository;
import com.pasteleria.pedido.model.EstadoPedido;
import com.pasteleria.pedido.model.Pedido;
import com.pasteleria.pedido.repository.PedidoRepository;
import com.pasteleria.producto.model.Producto;
import com.pasteleria.producto.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DetallePedidoService {

    private final DetallePedidoRepository detallePedidoRepository;
    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;

    public DetallePedidoService(DetallePedidoRepository detallePedidoRepository,
                                PedidoRepository pedidoRepository,
                                ProductoRepository productoRepository) {
        this.detallePedidoRepository = detallePedidoRepository;
        this.pedidoRepository = pedidoRepository;
        this.productoRepository = productoRepository;
    }

    // 1. Ver todas las líneas de un pedido
    public List<DetallePedido> obtenerPorPedido(Long pedidoId) {
        if (!pedidoRepository.existsById(pedidoId)) {
            throw new IllegalArgumentException("Pedido no encontrado con ID: " + pedidoId);
        }
        return detallePedidoRepository.findByPedidoId(pedidoId);
    }

    // 2. Ver una línea por su ID
    public DetallePedido obtenerPorId(Long id) {
        return detallePedidoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Detalle no encontrado con ID: " + id));
    }

    // 3. Agregar un producto a un pedido
    @Transactional
    public DetallePedido agregarDetalle(DetallePedidoRequest request) {
        // 3.1 Buscar el pedido
        Pedido pedido = pedidoRepository.findById(request.getPedidoId())
                .orElseThrow(() -> new IllegalArgumentException("Pedido no encontrado con ID: " + request.getPedidoId()));

        // 3.2 Solo se pueden agregar productos a pedidos pendientes
        if (pedido.getEstado() != EstadoPedido.PENDIENTE) {
            throw new IllegalStateException("Solo se pueden modificar pedidos en estado PENDIENTE");
        }

        // 3.3 Buscar el producto
        Producto producto = productoRepository.findById(request.getProductoId())
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + request.getProductoId()));

        // 3.4 Revisar stock
        if (producto.getStock() < request.getCantidad()) {
            throw new IllegalStateException("Stock insuficiente de " + producto.getNombre()
                    + ". Disponible: " + producto.getStock());
        }

        // 3.5 Armar la línea con el precio del producto (no del frontend)
        DetallePedido detalle = new DetallePedido();
        detalle.setPedido(pedido);
        detalle.setProducto(producto);
        detalle.setCantidad(request.getCantidad());
        detalle.setPrecioUnitario(producto.getPrecio());
        detalle.setSubtotal(producto.getPrecio() * request.getCantidad());

        // 3.6 Descontar stock
        producto.setStock(producto.getStock() - request.getCantidad());
        productoRepository.save(producto);

        // 3.7 Guardar la línea y actualizar el total del pedido
        DetallePedido guardado = detallePedidoRepository.save(detalle);
        recalcularTotal(pedido);

        return guardado;
    }

    // 4. Quitar una línea del pedido
    @Transactional
    public void eliminarDetalle(Long id) {
        DetallePedido detalle = obtenerPorId(id);
        Pedido pedido = detalle.getPedido();

        if (pedido.getEstado() != EstadoPedido.PENDIENTE) {
            throw new IllegalStateException("Solo se pueden modificar pedidos en estado PENDIENTE");
        }

        // Devolver el stock al producto
        Producto producto = detalle.getProducto();
        producto.setStock(producto.getStock() + detalle.getCantidad());
        productoRepository.save(producto);

        detallePedidoRepository.delete(detalle);
        recalcularTotal(pedido);
    }

    // 5. Sumar todas las líneas y aplicar el descuento del pedido
    private void recalcularTotal(Pedido pedido) {
        List<DetallePedido> detalles = detallePedidoRepository.findByPedidoId(pedido.getId());

        int suma = 0;
        for (DetallePedido d : detalles) {
            suma += d.getSubtotal();
        }

        int descuento = pedido.getDescuentoAplicado() != null ? pedido.getDescuentoAplicado() : 0;
        int total = suma - (suma * descuento / 100);

        pedido.setTotal(total);
        pedidoRepository.save(pedido);
    }
}