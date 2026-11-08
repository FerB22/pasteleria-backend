package cl.duoc.pasteleria_backend.service;

import cl.duoc.pasteleria_backend.DTO.PedidoRequestDTO;
import cl.duoc.pasteleria_backend.model.DetallePedido;
import cl.duoc.pasteleria_backend.model.EstadoPedido;
import cl.duoc.pasteleria_backend.model.Pedido;
import cl.duoc.pasteleria_backend.repository.PedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;

    public PedidoService(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    @Transactional
    public Pedido crearPedido(PedidoRequestDTO request) {
        Pedido pedido = Pedido.builder()
                .clienteId(request.getClienteId())
                .direccionEntrega(request.getDireccionEntrega())
                .metodoPago(request.getMetodoPago())
                .fechaCreacion(LocalDateTime.now())
                .estado(EstadoPedido.PENDIENTE)
                .build();

        BigDecimal acumuladoTotal = BigDecimal.ZERO;

        for (PedidoRequestDTO.ItemRequestDTO item : request.getItems()) {
            BigDecimal subtotal = item.getPrecioUnitario().multiply(BigDecimal.valueOf(item.getCantidad()));
            acumuladoTotal = acumuladoTotal.add(subtotal);

            DetallePedido detalle = DetallePedido.builder()
                    .productoId(item.getProductoId())
                    .cantidad(item.getCantidad())
                    .precioUnitario(item.getPrecioUnitario())
                    .subtotal(subtotal)
                    .build();

            pedido.agregarDetalle(detalle);
        }

        pedido.setTotal(acumuladoTotal);
        return pedidoRepository.save(pedido);
    }

    public List<Pedido> listarPedidos(String clienteId) {
        if (clienteId != null && !clienteId.trim().isEmpty()) {
            return pedidoRepository.findByClienteId(clienteId);
        }
        return pedidoRepository.findAll();
    }

    public Pedido obtenerPorId(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con ID: " + id));
    }

    @Transactional
    public Pedido actualizarEstado(Long id, EstadoPedido nuevoEstado) {
        Pedido pedido = obtenerPorId(id);
        pedido.setEstado(nuevoEstado);
        return pedidoRepository.save(pedido);
    }
}