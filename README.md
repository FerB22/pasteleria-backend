# Microservicio de Usuarios y Beneficios — Pastelería Mil Sabores

Microservicio backend desarrollado con **Spring Boot 3.3.4** y **Java 21**, encargado de la persistencia relacional de usuarios, autenticación y cálculo de beneficios comerciales.

---

## 1. Arquitectura y tecnologías

- **Lenguaje**: Java 21 (LTS).
- **Framework**: Spring Boot 3.3.4 (`spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-validation`).
- **Base de datos de desarrollo**: H2 Database en memoria (`jdbc:h2:mem:usuariosdb`).
- **Documentación interactiva**: SpringDoc OpenAPI 2.6.0 (Swagger UI).
- **Pruebas**: JUnit 5 y Mockito.
- **Puerto de red**: `8081` (para convivir armónicamente con los microservicios de productos y pedidos del equipo).

---

## 2. Puntos de acceso (Endpoints REST)

| Método | Endpoint | Descripción | Código de éxito |
| :--- | :--- | :--- | :--- |
| `POST` | `/api/usuarios/registro` | Registra un nuevo usuario con validación Módulo 11 de RUT y correo | `201 Created` |
| `POST` | `/api/usuarios/login` | Autentica un usuario por correo y contraseña | `200 OK` |
| `GET` | `/api/usuarios` | Lista todos los usuarios registrados | `200 OK` |
| `GET` | `/api/usuarios/{id}` | Obtiene los datos de un usuario por su ID | `200 OK` |
| `GET` | `/api/usuarios/{id}/beneficios` | Consulta el porcentaje de descuento y beneficios aplicables | `200 OK` |
| `PUT` | `/api/usuarios/{id}` | Actualiza datos personales del usuario | `200 OK` |
| `DELETE` | `/api/usuarios/{id}` | Elimina un usuario por su ID | `200 OK` |

---

## 3. Beneficios comerciales implementados

1. **50 % de descuento (Bodas de oro)**: Se aplica de forma automática si la edad calculada a partir de `fechaNacimiento` es igual o superior a 50 años.
2. **10 % de descuento por cupón promocional**: Se aplica al enviar el parámetro `?cupon=FELICES50` para clientes menores de 50 años.
3. **Convenio Duoc UC**: Identifica usuarios con correos `@duocuc.cl` o `@alumnos.duoc.cl` para entregar torta de cumpleaños gratis durante el mes natalicio.

---

## 4. Datos de prueba precargados (`DataInitializer`)

Al iniciar la aplicación, se insertan automáticamente tres perfiles de prueba en H2:

1. **Administrador**: `admin@milsabores.cl` | Contraseña: `admin123` | RUT: `11111111-1`
2. **Cliente Senior (58 años)**: `marta.gonzalez@gmail.com` | Contraseña: `marta2026` | RUT: `15345678-5` (Aplica 50 % de descuento).
3. **Cliente Duoc UC (23 años)**: `f.barra@alumnos.duoc.cl` | Contraseña: `duoc2026` | RUT: `20123456-7` (Aplica convenio Duoc UC).

---

## 5. Accesos del entorno local

- **Consola web H2**: `http://localhost:8081/h2-console`
  - JDBC URL: `jdbc:h2:mem:usuariosdb`
  - Usuario: `sa`
  - Contraseña: *(vacía)*
- **Documentación Swagger UI**: `http://localhost:8081/swagger-ui.html`
