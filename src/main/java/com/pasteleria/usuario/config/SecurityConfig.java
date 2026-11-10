package com.pasteleria.usuario.config;

import com.pasteleria.usuario.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.nio.charset.StandardCharsets;

/**
 * Configuración de Spring Security: API sin estado (stateless) protegida con JWT y roles.
 *
 * Rutas públicas: registro, login, Swagger y consola H2.
 * Rutas con rol: listar y eliminar usuarios solo para ADMIN.
 * Resto de rutas de usuarios: requieren token válido (la regla fina de "dueño o ADMIN"
 * se aplica con @PreAuthorize en el controlador).
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    /** Prefijos de ruta vigentes: la versión oficial v1 y la ruta histórica sin versión. */
    private static final String[] BASES = {"/api/v1/usuarios", "/api/usuarios"};

    private final JwtAuthenticationFilter jwtFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .headers(h -> h.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin))
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, res, ex) ->
                                escribirError(res, 401, "Autenticación requerida: token ausente, inválido o expirado"))
                        .accessDeniedHandler((req, res, ex) ->
                                escribirError(res, 403, "Acceso denegado: su rol no tiene permiso para este recurso")))
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers(
                            "/swagger-ui.html", "/swagger-ui/**", "/api-docs/**", "/v3/api-docs/**",
                            "/h2-console/**", "/error").permitAll();
                    auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();
                    for (String base : BASES) {
                        auth.requestMatchers(HttpMethod.POST, base + "/registro", base + "/login").permitAll();
                        auth.requestMatchers(HttpMethod.GET, base).hasRole("ADMIN");
                        auth.requestMatchers(HttpMethod.DELETE, base + "/**").hasRole("ADMIN");
                        auth.requestMatchers(base + "/**").authenticated();
                    }
                    auth.anyRequest().authenticated();
                })
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private static void escribirError(jakarta.servlet.http.HttpServletResponse res, int estado, String mensaje)
            throws java.io.IOException {
        res.setStatus(estado);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding(StandardCharsets.UTF_8.name());
        res.getWriter().write("{\"error\": \"" + mensaje + "\"}");
    }
}
