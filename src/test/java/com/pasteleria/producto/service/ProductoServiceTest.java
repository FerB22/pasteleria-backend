package com.pasteleria.producto.service;

import com.pasteleria.producto.model.Producto;
import com.pasteleria.producto.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias para ProductoService.
 * Verifica normalización de datos, unicidad de código y validaciones defensivas.
 */
@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private ProductoService productoService;

    private Producto tortaBase;

    @BeforeEach
    void setUp() {
        tortaBase = new Producto(
                "tc001",
                "  Torta Cuadrada de Chocolate  ",
                "Bizcocho húmedo relleno de ganache.",
                45000,
                10,
                "Cuadradas",
                "/images/torta-cuadrada-chocolate.jpg"
        );
        tortaBase.setId(1L);
    }

    @Test
    @DisplayName("Debe crear producto válido normalizando código en mayúsculas y categoría en minúsculas")
    void testCrearProductoValido() {
        when(productoRepository.existsByCodigoIgnoreCase("TC001")).thenReturn(false);
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        Producto creado = productoService.crearProducto(tortaBase);

        assertEquals("TC001", creado.getCodigo());
        assertEquals("Torta Cuadrada de Chocolate", creado.getNombre());
        assertEquals("cuadradas", creado.getCategoria());
        verify(productoRepository).save(tortaBase);
    }

    @Test
    @DisplayName("Debe rechazar creación cuando el código ya existe")
    void testCrearProductoCodigoDuplicado() {
        when(productoRepository.existsByCodigoIgnoreCase("TC001")).thenReturn(true);

        assertThrows(IllegalStateException.class, () -> productoService.crearProducto(tortaBase));
    }

    @Test
    @DisplayName("Debe rechazar producto con precio negativo o nulo")
    void testCrearProductoPrecioInvalido() {
        tortaBase.setPrecio(-100);
        assertThrows(IllegalArgumentException.class, () -> productoService.crearProducto(tortaBase));

        tortaBase.setPrecio(null);
        assertThrows(IllegalArgumentException.class, () -> productoService.crearProducto(tortaBase));
    }

    @Test
    @DisplayName("Debe rechazar producto con stock negativo")
    void testCrearProductoStockNegativo() {
        tortaBase.setStock(-1);
        assertThrows(IllegalArgumentException.class, () -> productoService.crearProducto(tortaBase));
    }

    @Test
    @DisplayName("Debe rechazar producto con código en blanco")
    void testCrearProductoCodigoEnBlanco() {
        tortaBase.setCodigo("   ");
        assertThrows(IllegalArgumentException.class, () -> productoService.crearProducto(tortaBase));
    }

    @Test
    @DisplayName("Debe actualizar todos los campos incluyendo codigo e imagenUrl")
    void testActualizarProductoCompleto() {
        Producto cambios = new Producto(
                "tc002",
                "Torta Actualizada",
                "Nueva descripción",
                48000,
                8,
                "Especiales",
                "/images/torta-boda.jpg"
        );

        when(productoRepository.findById(1L)).thenReturn(Optional.of(tortaBase));
        when(productoRepository.existsByCodigoIgnoreCase("TC002")).thenReturn(false);
        when(productoRepository.save(any(Producto.class))).thenAnswer(inv -> inv.getArgument(0));

        Producto actualizado = productoService.actualizarProducto(1L, cambios);

        assertEquals("TC002", actualizado.getCodigo());
        assertEquals("Torta Actualizada", actualizado.getNombre());
        assertEquals("especiales", actualizado.getCategoria());
        assertEquals("/images/torta-boda.jpg", actualizado.getImagenUrl());
        assertEquals(48000, actualizado.getPrecio());
        assertEquals(8, actualizado.getStock());
    }

    @Test
    @DisplayName("Debe lanzar NoSuchElementException al eliminar producto inexistente")
    void testEliminarProductoInexistente() {
        when(productoRepository.existsById(99L)).thenReturn(false);

        assertThrows(NoSuchElementException.class, () -> productoService.eliminarProducto(99L));
    }
}
