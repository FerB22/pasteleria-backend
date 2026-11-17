package com.pasteleria.detalle_pedido.service;

import com.pasteleria.detalle_pedido.model.DetallePedido;
import com.pasteleria.detalle_pedido.model.DetallePedidoRequest;
import com.pasteleria.detalle_pedido.repository.DetallePedidoRepository;
import com.pasteleria.pedido.model.EstadoPedido;
import com.pasteleria.pedido.model.Pedido;
import com.pasteleria.pedido.repository.PedidoRepository;
import com.pasteleria.producto.model.Producto;
import com.pasteleria.producto.repository.ProductoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DetallePedidoServiceTest {

    @Mock
    private DetallePedidoRepository detallePedidoRepository;

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private DetallePedidoService detallePedidoService;

    @Test
    void testAgregarDetalleDescuentaStockYRecalculaTotalConDescuento() {
        Pedido pedido = new Pedido();
        pedido.setId(1L);
        pedido.setEstado(EstadoPedido.PENDIENTE);
        pedido.setDescuentoAplicado(10);

        Producto producto = new Producto();
        producto.setId(2L);
        producto.setNombre("Torta Tres Leches");
        producto.setPrecio(20000);
        producto.setStock(5);

        DetallePedidoRequest req = new DetallePedidoRequest();
        req.setPedidoId(1L);
        req.setProductoId(2L);
        req.setCantidad(2);

        DetallePedido lineaGuardada = new DetallePedido();
        lineaGuardada.setPedido(pedido);
        lineaGuardada.setProducto(producto);
        lineaGuardada.setCantidad(2);
        lineaGuardada.setPrecioUnitario(20000);
        lineaGuardada.setSubtotal(40000);

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(productoRepository.findById(2L)).thenReturn(Optional.of(producto));
        when(detallePedidoRepository.save(any(DetallePedido.class))).thenReturn(lineaGuardada);
        when(detallePedidoRepository.findByPedidoId(1L)).thenReturn(List.of(lineaGuardada));

        DetallePedido resultado = detallePedidoService.agregarDetalle(req);

        assertEquals(20000, resultado.getPrecioUnitario());
        assertEquals(40000, resultado.getSubtotal());
        assertEquals(3, producto.getStock());
        assertEquals(36000, pedido.getTotal());
        verify(productoRepository).save(producto);
        verify(pedidoRepository).save(pedido);
    }

    @Test
    void testAgregarDetalleStockInsuficiente() {
        Pedido pedido = new Pedido();
        pedido.setId(1L);
        pedido.setEstado(EstadoPedido.PENDIENTE);

        Producto producto = new Producto();
        producto.setId(2L);
        producto.setNombre("Kuchen");
        producto.setStock(1);

        DetallePedidoRequest req = new DetallePedidoRequest();
        req.setPedidoId(1L);
        req.setProductoId(2L);
        req.setCantidad(3);

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(productoRepository.findById(2L)).thenReturn(Optional.of(producto));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> detallePedidoService.agregarDetalle(req));
        assertTrue(ex.getMessage().contains("Stock insuficiente"));
        verify(detallePedidoRepository, never()).save(any());
    }

    @Test
    void testAgregarDetallePedidoNoPendiente() {
        Pedido pedido = new Pedido();
        pedido.setId(1L);
        pedido.setEstado(EstadoPedido.ENTREGADO);

        DetallePedidoRequest req = new DetallePedidoRequest();
        req.setPedidoId(1L);
        req.setProductoId(2L);
        req.setCantidad(1);

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));

        assertThrows(IllegalStateException.class, () -> detallePedidoService.agregarDetalle(req));
    }

    @Test
    void testEliminarDetalleDevuelveStockYRecalculaTotal() {
        Pedido pedido = new Pedido();
        pedido.setId(1L);
        pedido.setEstado(EstadoPedido.PENDIENTE);
        pedido.setDescuentoAplicado(0);

        Producto producto = new Producto();
        producto.setId(2L);
        producto.setStock(4);

        DetallePedido detalle = new DetallePedido();
        detalle.setId(10L);
        detalle.setPedido(pedido);
        detalle.setProducto(producto);
        detalle.setCantidad(2);

        when(detallePedidoRepository.findById(10L)).thenReturn(Optional.of(detalle));
        when(detallePedidoRepository.findByPedidoId(1L)).thenReturn(List.of());

        detallePedidoService.eliminarDetalle(10L);

        assertEquals(6, producto.getStock());
        assertEquals(0, pedido.getTotal());
        verify(detallePedidoRepository).delete(detalle);
        verify(productoRepository).save(producto);
    }
}
