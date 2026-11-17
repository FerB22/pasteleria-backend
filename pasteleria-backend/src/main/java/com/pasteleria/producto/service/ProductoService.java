package com.pasteleria.producto.service;

import com.pasteleria.producto.model.Producto;
import com.pasteleria.producto.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    public List<Producto> obtenerTodos() {
        return productoRepository.findAll();
    }

    public Optional<Producto> obtenerPorId(Long id) {
        return productoRepository.findById(id);
    }

    public Producto crearProducto(Producto producto) {
        if (producto.getPrecio() == null || producto.getPrecio() < 0) {
            throw new IllegalArgumentException("El precio del producto no puede ser negativo ni nulo.");
        }
        if (producto.getStock() == null || producto.getStock() < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo ni nulo.");
        }
        return productoRepository.save(producto);
    }

    public Producto actualizarProducto(Long id, Producto detalles) {
        return productoRepository.findById(id).map(productoExistente -> {
            productoExistente.setNombre(detalles.getNombre());
            productoExistente.setDescripcion(detalles.getDescripcion());
            productoExistente.setPrecio(detalles.getPrecio());
            productoExistente.setStock(detalles.getStock());
            productoExistente.setCategoria(detalles.getCategoria());
            return productoRepository.save(productoExistente);
        }).orElseThrow(() -> new RuntimeException("Producto no encontrado con el ID: " + id));
    }

    public void eliminarProducto(Long id) {
        if (productoRepository.existsById(id)) {
            productoRepository.deleteById(id);
        } else {
            throw new RuntimeException("No se puede eliminar: Producto no encontrado.");
        }
    }
}