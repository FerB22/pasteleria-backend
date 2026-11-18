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
        validar(producto);
        producto.setCodigo(producto.getCodigo().trim().toUpperCase());
        if (productoRepository.existsByCodigoIgnoreCase(producto.getCodigo())) {
            throw new IllegalArgumentException("Ya existe un producto con el código " + producto.getCodigo() + ".");
        }
        producto.setId(null);
        return productoRepository.save(producto);
    }

    public Producto actualizarProducto(Long id, Producto detalles) {
        Producto productoExistente = productoRepository.findById(id)
                .orElseThrow(() -> new java.util.NoSuchElementException("Producto no encontrado con el ID: " + id));

        validar(detalles);
        String codigo = detalles.getCodigo().trim().toUpperCase();
        if (productoRepository.existsByCodigoIgnoreCaseAndIdNot(codigo, id)) {
            throw new IllegalArgumentException("Ya existe un producto con el código " + codigo + ".");
        }

        productoExistente.setCodigo(codigo);
        productoExistente.setNombre(detalles.getNombre());
        productoExistente.setDescripcion(detalles.getDescripcion());
        productoExistente.setPrecio(detalles.getPrecio());
        productoExistente.setStock(detalles.getStock());
        productoExistente.setCategoria(detalles.getCategoria());
        productoExistente.setImagenUrl(detalles.getImagenUrl());
        return productoRepository.save(productoExistente);
    }

    public void eliminarProducto(Long id) {
        if (!productoRepository.existsById(id)) {
            throw new java.util.NoSuchElementException("No se puede eliminar: Producto no encontrado.");
        }
        productoRepository.deleteById(id);
        // Si el producto aparece en un pedido, la base de datos lo rechaza al confirmar
        productoRepository.flush();
    }

    // Revisa que los datos obligatorios estén completos y sean válidos
    private void validar(Producto producto) {
        if (producto.getCodigo() == null || producto.getCodigo().isBlank()) {
            throw new IllegalArgumentException("Debe ingresar el código del producto.");
        }
        if (producto.getNombre() == null || producto.getNombre().isBlank()) {
            throw new IllegalArgumentException("Debe ingresar el nombre del producto.");
        }
        if (producto.getCategoria() == null || producto.getCategoria().isBlank()) {
            throw new IllegalArgumentException("Debe seleccionar una categoría.");
        }
        if (producto.getPrecio() == null || producto.getPrecio() <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor a 0.");
        }
        if (producto.getStock() == null || producto.getStock() < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo ni nulo.");
        }
    }
}
