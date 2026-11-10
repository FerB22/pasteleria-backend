package com.pasteleria.usuario.security;

/**
 * Identidad del usuario ya autenticado, reconstruida a partir de las claims del JWT.
 * Se guarda como principal en el SecurityContext y se consulta desde @PreAuthorize.
 */
public class UsuarioAutenticado {

    private final Long id;
    private final String email;
    private final String rol;

    public UsuarioAutenticado(Long id, String email, String rol) {
        this.id = id;
        this.email = email;
        this.rol = rol;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getRol() {
        return rol;
    }
}
