package com.pasteleria.usuario.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

/**
 * Entidad JPA que representa a un usuario en el sistema de la pastelería.
 * Aplica restricciones relacionales de unicidad para RUT y correo electrónico.
 */
@Entity
@Table(name = "usuarios")
@Schema(description = "Modelo de entidad de Usuario para autenticación, gestión y beneficios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "Identificador único autogenerado", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank(message = "El RUT es obligatorio")
    @Size(max = 12, message = "El RUT no debe exceder 12 caracteres")
    @Column(name = "rut", nullable = false, unique = true, length = 12)
    @Schema(description = "RUT chileno con formato o sin puntos, validado por Módulo 11", example = "19.876.543-0")
    private String rut;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no debe exceder 100 caracteres")
    @Column(name = "nombre", nullable = false, length = 100)
    @Schema(description = "Nombre de pila del usuario", example = "Fernando")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 100, message = "El apellido no debe exceder 100 caracteres")
    @Column(name = "apellido", nullable = false, length = 100)
    @Schema(description = "Apellido del usuario", example = "Barra")
    private String apellido;

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El formato del correo electrónico es inválido")
    @Size(max = 150, message = "El correo no debe exceder 150 caracteres")
    @Column(name = "email", nullable = false, unique = true, length = 150)
    @Schema(description = "Correo electrónico personal o institucional", example = "f.barra@alumnos.duoc.cl")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, max = 255, message = "La contraseña debe tener al menos 6 caracteres")
    @Column(name = "password", nullable = false, length = 255)
    @Schema(description = "Contraseña de acceso al sistema", example = "contrasenaSegura2026")
    private String password;

    @Size(max = 20, message = "El teléfono no debe exceder 20 caracteres")
    @Column(name = "telefono", length = 20)
    @Schema(description = "Número telefónico de contacto", example = "+56 9 8765 4321")
    private String telefono;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @Column(name = "fecha_nacimiento", nullable = false)
    @Schema(description = "Fecha de nacimiento para cálculo dinámico de beneficios (AAAA-MM-DD)", example = "2003-05-15")
    private LocalDate fechaNacimiento;

    @Column(name = "rol", nullable = false, length = 30)
    @Schema(description = "Rol del usuario en el sistema", allowableValues = {"ADMIN", "VENDEDOR", "CLIENTE"}, example = "CLIENTE")
    private String rol = "CLIENTE";

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    @Schema(description = "Fecha y hora de registro automático en base de datos", example = "2026-10-27T14:30:00", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime fechaRegistro;

    public Usuario() {
    }

    public Usuario(String rut, String nombre, String apellido, String email, String password, String telefono, LocalDate fechaNacimiento, String rol) {
        this.rut = rut;
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.password = password;
        this.telefono = telefono;
        this.fechaNacimiento = fechaNacimiento;
        this.rol = (rol != null && !rol.isBlank()) ? rol : "CLIENTE";
    }

    @PrePersist
    public void prePersist() {
        if (this.fechaRegistro == null) {
            this.fechaRegistro = LocalDateTime.now();
        }
        if (this.rol == null || this.rol.isBlank()) {
            this.rol = "CLIENTE";
        }
    }

    // Métodos de cálculo dinámico del dominio (sin redundancia en base de datos)

    /**
     * Calcula la edad en años cumplidos en tiempo de ejecución.
     */
    public int calcularEdad() {
        if (this.fechaNacimiento == null) {
            return 0;
        }
        return Period.between(this.fechaNacimiento, LocalDate.now()).getYears();
    }

    /**
     * Determina si el cliente califica para el 50 % de descuento por 50 años o más.
     */
    public boolean calificaParaDescuentoCincuentaAnos() {
        return calcularEdad() >= 50;
    }

    /**
     * Determina si el usuario posee correo institucional Duoc UC.
     */
    public boolean esUsuarioDuoc() {
        if (this.email == null || this.email.isBlank()) {
            return false;
        }
        String correoLimpio = this.email.trim().toLowerCase();
        return correoLimpio.endsWith("@duocuc.cl") || correoLimpio.endsWith("@alumnos.duoc.cl");
    }

    // Getters y Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRut() {
        return rut;
    }

    public void setRut(String rut) {
        this.rut = rut;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
}
