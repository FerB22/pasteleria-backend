package com.pasteleria.producto.repository;

import com.pasteleria.producto.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    boolean existsByCodigoIgnoreCase(String codigo);

    Optional<Producto> findByCodigoIgnoreCase(String codigo);

    List<Producto> findByCategoriaIgnoreCase(String categoria);
}
