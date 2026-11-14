package com.pasteleria.producto.service;

import com.pasteleria.producto.model.Producto;
import com.pasteleria.producto.repository.ProductoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Lógica de negocio y validaciones defensivas para el catálogo de productos.
 */
@Service
public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    public List<Producto> obtenerTodos() {
        return productoRepository.findAll();
    }

    public List<Producto> obtenerPorCategoria(String categoria) {
        return productoRepository.findByCategoriaIgnoreCase(categoria.trim());
    }

    public Optional<Producto> obtenerPorId(Long id) {
        return productoRepository.findById(id);
    }

    public Producto crearProducto(Producto producto) {
        validarDatosProducto(producto);
        String codigoNormalizado = producto.getCodigo().trim().toUpperCase();

        if (productoRepository.existsByCodigoIgnoreCase(codigoNormalizado)) {
            throw new IllegalStateException("Ya existe un producto registrado con el código: " + codigoNormalizado);
        }

        producto.setCodigo(codigoNormalizado);
        producto.setNombre(producto.getNombre().trim());
        producto.setCategoria(producto.getCategoria().trim().toLowerCase());
        return productoRepository.save(producto);
    }

    public Producto actualizarProducto(Long id, Producto detalles) {
        validarDatosProducto(detalles);
        String nuevoCodigo = detalles.getCodigo().trim().toUpperCase();

        Producto productoExistente = productoRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Producto no encontrado con el ID: " + id));

        boolean cambiaCodigo = productoExistente.getCodigo() == null
                || !productoExistente.getCodigo().equalsIgnoreCase(nuevoCodigo);

        if (cambiaCodigo && productoRepository.existsByCodigoIgnoreCase(nuevoCodigo)) {
            throw new IllegalStateException("Ya existe otro producto con el código: " + nuevoCodigo);
        }

        productoExistente.setCodigo(nuevoCodigo);
        productoExistente.setNombre(detalles.getNombre().trim());
        productoExistente.setDescripcion(detalles.getDescripcion());
        productoExistente.setPrecio(detalles.getPrecio());
        productoExistente.setStock(detalles.getStock());
        productoExistente.setCategoria(detalles.getCategoria().trim().toLowerCase());
        productoExistente.setImagenUrl(detalles.getImagenUrl());

        return productoRepository.save(productoExistente);
    }

    public void eliminarProducto(Long id) {
        if (!productoRepository.existsById(id)) {
            throw new NoSuchElementException("No se puede eliminar: producto no encontrado con el ID: " + id);
        }
        productoRepository.deleteById(id);
    }

    private void validarDatosProducto(Producto producto) {
        if (producto == null) {
            throw new IllegalArgumentException("Los datos del producto son obligatorios.");
        }
        if (producto.getCodigo() == null || producto.getCodigo().isBlank()) {
            throw new IllegalArgumentException("El código del producto es obligatorio.");
        }
        if (producto.getNombre() == null || producto.getNombre().isBlank()) {
            throw new IllegalArgumentException("El nombre del producto es obligatorio.");
        }
        if (producto.getCategoria() == null || producto.getCategoria().isBlank()) {
            throw new IllegalArgumentException("La categoría del producto es obligatoria.");
        }
        if (producto.getPrecio() == null || producto.getPrecio() < 0) {
            throw new IllegalArgumentException("El precio del producto no puede ser negativo ni nulo.");
        }
        if (producto.getStock() == null || producto.getStock() < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo ni nulo.");
        }
    }
}
