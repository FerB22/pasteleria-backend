package com.pasteleria.producto.config;

import com.pasteleria.producto.model.Producto;
import com.pasteleria.producto.repository.ProductoRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ProductoDataInitializer implements CommandLineRunner {

    private final ProductoRepository productoRepository;

    public ProductoDataInitializer(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    @Override
    public void run(String... args) {
        if (productoRepository.count() == 0) {
            Producto torta1 = new Producto();
            torta1.setNombre("Torta Tres Leches");
            torta1.setDescripcion("Clásica torta húmeda rellena con manjar casero.");
            torta1.setPrecio(15000);
            torta1.setStock(10);
            torta1.setCategoria("Torta");

            Producto kuchen1 = new Producto();
            kuchen1.setNombre("Kuchen de Nuez");
            kuchen1.setDescripcion("Kuchen tradicional sureño con nueces y caramelo.");
            kuchen1.setPrecio(12000);
            kuchen1.setStock(5);
            kuchen1.setCategoria("Kuchen");

            productoRepository.save(torta1);
            productoRepository.save(kuchen1);
            
            System.out.println("✅ Datos de prueba de Productos cargados exitosamente.");
        }
    }
}