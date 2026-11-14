package com.pasteleria.producto.config;

import com.pasteleria.producto.model.Producto;
import com.pasteleria.producto.repository.ProductoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Carga inicial de respaldo en caso de que la tabla de productos esté vacía al arrancar.
 * Nota: Si data.sql ya insertó productos al iniciar la BD, este bloque no duplica registros.
 */
@Component
public class ProductoDataInitializer implements CommandLineRunner {

    private final ProductoRepository productoRepository;

    public ProductoDataInitializer(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @Override
    public void run(String... args) {
        if (productoRepository.count() == 0) {
            Producto torta1 = new Producto(
                    "TT001",
                    "Torta Tres Leches",
                    "Clásica torta húmeda rellena con manjar casero.",
                    15000,
                    10,
                    "circulares",
                    "/images/torta-circular-manjar.jpg"
            );

            Producto kuchen1 = new Producto(
                    "PT001",
                    "Kuchen de Nuez",
                    "Kuchen tradicional sureño con nueces y caramelo.",
                    12000,
                    5,
                    "tradicional",
                    "/images/empanada-manzana.jpg"
            );

            productoRepository.save(torta1);
            productoRepository.save(kuchen1);
            System.out.println("Datos iniciales de productos cargados correctamente.");
        }
    }
}
