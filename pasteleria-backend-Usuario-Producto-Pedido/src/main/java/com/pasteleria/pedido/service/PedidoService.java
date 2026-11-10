package com.pasteleria.pedido.service;

import com.pasteleria.pedido.model.EstadoPedido;
import com.pasteleria.pedido.model.Pedido;
import com.pasteleria.pedido.model.PedidoRequest;
import com.pasteleria.pedido.repository.PedidoRepository;
import com.pasteleria.usuario.model.Usuario;
import com.pasteleria.usuario.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;

    public PedidoService(PedidoRepository pedidoRepository, UsuarioRepository usuarioRepository) {
        this.pedidoRepository = pedidoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<Pedido> obtenerTodos() {
        return pedidoRepository.findAll();
    }

    public Pedido obtenerPorId(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con ID: " + id));
    }

    public List<Pedido> obtenerPorUsuario(Long usuarioId) {
        return pedidoRepository.findByUsuarioId(usuarioId);
    }

    @Transactional
    public Pedido crearPedido(PedidoRequest request) {
        Usuario usuario = usuarioRepository.findById(request.getUsuarioId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + request.getUsuarioId()));

        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);
        pedido.setTotal(request.getTotal());
        pedido.setDescuentoAplicado(request.getDescuentoAplicado() != null ? request.getDescuentoAplicado() : 0);
        pedido.setDireccionDespacho(request.getDireccionDespacho());
        pedido.setMetodoPago(request.getMetodoPago());

        return pedidoRepository.save(pedido);
    }

    @Transactional
    public Pedido actualizarEstado(Long id, EstadoPedido nuevoEstado) {
        Pedido pedido = obtenerPorId(id);
        pedido.setEstado(nuevoEstado);
        return pedidoRepository.save(pedido);
    }

    public void eliminarPedido(Long id) {
        Pedido pedido = obtenerPorId(id);
        pedidoRepository.delete(pedido);
    }
}