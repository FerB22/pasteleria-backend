package com.pasteleria.usuario.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro ligero que registra en consola cada petición HTTP (método, ruta, estado y duración).
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class HttpLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        long inicio = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long duracion = System.currentTimeMillis() - inicio;
            String query = request.getQueryString();
            String ruta = query == null ? request.getRequestURI() : request.getRequestURI() + "?" + query;
            System.out.printf("[HTTP] %s %s -> %d (%d ms)%n",
                    request.getMethod(),
                    ruta,
                    response.getStatus(),
                    duracion);
        }
    }
}
