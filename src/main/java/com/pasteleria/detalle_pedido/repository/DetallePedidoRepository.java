package com.pasteleria.detalle_pedido.repository;

import com.pasteleria.detalle_pedido.model.DetallePedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Long> {

    @Query("SELECT d FROM DetallePedido d WHERE d.pedido.id = :pedidoId")
    List<DetallePedido> findByPedidoId(@Param("pedidoId") Long pedidoId);
}
