package cl.duoc.pasteleria_backend.repository;

import cl.duoc.pasteleria_backend.model.EstadoPedido;
import cl.duoc.pasteleria_backend.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByClienteId(String clienteId);
    List<Pedido> findByEstado(EstadoPedido estado);
}