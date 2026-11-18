package com.pasteleria.producto.repository;

import com.pasteleria.producto.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // ¿Existe otro producto con ese código? (al crear)
    boolean existsByCodigoIgnoreCase(String codigo);

    // ¿Existe otro producto con ese código, distinto al que se edita? (al actualizar)
    boolean existsByCodigoIgnoreCaseAndIdNot(String codigo, Long id);
}
