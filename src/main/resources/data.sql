-- ==============================================================================
-- Script de Datos de Prueba Iniciales: Pastelería Mil Sabores
-- ==============================================================================

-- 1. Usuarios (Administrador, Cliente Senior y Estudiante Duoc UC)
INSERT INTO usuarios (rut, nombre, apellido, email, password, telefono, fecha_nacimiento, rol)
VALUES 
('11111111-1', 'Alonso', 'Contreras', 'admin@milsabores.cl', 'admin123', '+56911223344', '1995-03-15', 'ADMIN'),
('15345678-5', 'Marta', 'González', 'marta.gonzalez@gmail.com', 'marta2026', '+56987654321', '1968-05-20', 'CLIENTE'),
('20123456-7', 'Fernando', 'Barra', 'f.barra@alumnos.duoc.cl', 'duoc2026', '+56955667788', '2003-10-15', 'CLIENTE');

-- 2. Catálogo inicial de pastelería (para Benjamín)
INSERT INTO productos (codigo, nombre, descripcion, precio, stock, categoria, imagen_url)
VALUES 
('TOR-001', 'Torta Mil Hojas Manjar', 'Capas crujientes de hojarasca con manjar casero', 22500, 15, 'Tortas Tradicionales', '/img/milhojas.jpg'),
('TOR-002', 'Torta Selva Negra', 'Bizcocho de chocolate con crema chantilly y guindas', 24990, 10, 'Tortas Especiales', '/img/selvanegra.jpg'),
('PAS-001', 'Pie de Limón Merengado', 'Masa quebrada con crema de limón de pica y merengue suizo', 14500, 20, 'Tartas y Pies', '/img/pielimon.jpg'),
('PAS-002', 'Cheesecake Frutos Rojos', 'Base de galleta con crema de queso y salsa de frambuesas', 18900, 12, 'Postres', '/img/cheesecake.jpg');

-- 3. Pedido de ejemplo (para Alonso y Magdalena)
INSERT INTO pedidos (usuario_id, estado, total, descuento_aplicado, direccion_despacho, metodo_pago)
VALUES 
(2, 'ENTREGADO', 22500, 50, 'Av. Libertador Bernardo O''Higgins 1234, Santiago', 'TRANSFERENCIA');

-- 4. Detalle del pedido de ejemplo
INSERT INTO detalle_pedidos (pedido_id, producto_id, cantidad, precio_unitario, subtotal)
VALUES 
(1, 1, 1, 22500, 22500);
