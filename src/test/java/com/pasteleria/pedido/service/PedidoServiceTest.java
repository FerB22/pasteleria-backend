package com.pasteleria.pedido.service;

import com.pasteleria.pedido.model.EstadoPedido;
import com.pasteleria.pedido.model.Pedido;
import com.pasteleria.pedido.model.PedidoRequest;
import com.pasteleria.pedido.repository.PedidoRepository;
import com.pasteleria.usuario.model.Usuario;
import com.pasteleria.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private PedidoService pedidoService;

    @Test
    void testCrearPedidoExitoso() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);

        PedidoRequest req = new PedidoRequest();
        req.setUsuarioId(1L);
        req.setTotal(15000);
        req.setDescuentoAplicado(10);
        req.setDireccionDespacho("Av. Providencia 1234");
        req.setMetodoPago("DEBITO");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        Pedido creado = pedidoService.crearPedido(req);

        assertEquals(usuario, creado.getUsuario());
        assertEquals(EstadoPedido.PENDIENTE, creado.getEstado());
        assertEquals(15000, creado.getTotal());
        assertEquals(10, creado.getDescuentoAplicado());
        verify(pedidoRepository).save(any(Pedido.class));
    }

    @Test
    void testCrearPedidoUsuarioInexistente() {
        PedidoRequest req = new PedidoRequest();
        req.setUsuarioId(99L);

        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> pedidoService.crearPedido(req));
        assertTrue(ex.getMessage().contains("Usuario no encontrado"));
        verify(pedidoRepository, never()).save(any());
    }

    @Test
    void testActualizarEstadoExitoso() {
        Pedido pedido = new Pedido();
        pedido.setId(5L);
        pedido.setEstado(EstadoPedido.PENDIENTE);

        when(pedidoRepository.findById(5L)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> inv.getArgument(0));

        Pedido actualizado = pedidoService.actualizarEstado(5L, EstadoPedido.EN_PREPARACION);

        assertEquals(EstadoPedido.EN_PREPARACION, actualizado.getEstado());
    }

    @Test
    void testObtenerPorIdInexistente() {
        when(pedidoRepository.findById(404L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> pedidoService.obtenerPorId(404L));
        assertTrue(ex.getMessage().contains("Pedido no encontrado"));
    }
}
