package com.gestionHoteles.TFG.Listener;

import com.gestionHoteles.TFG.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RequestListener extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // Obtener la URI de la petición
        String uri = request.getRequestURI();
        System.out.println("URI solicitada: " + uri);

        // Comprobar si es una ruta privada
        if (uri.startsWith("/api/private")) {
            // Leer el header Authorization
            String authHeader = request.getHeader("Authorization");
            System.out.println("Authorization Header: " + authHeader);

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Token no proporcionado o inválido");
                return;
            }

            // Extraer el token (elimina el "Bearer ")
            String token = authHeader.substring(7);
            System.out.println("Token recibido: " + token);

            // Validar el token utilizando el JwtUtil
            JwtUtil jwtUtil = new JwtUtil();

            try {
                if (!jwtUtil.validateToken(token)) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.getWriter().write("Token inválido o expirado");
                    return;
                }
            } catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("Token no válido");
                return;
            }
        }

        // Continuar con la cadena de filtros si todo está bien
        filterChain.doFilter(request, response);
    }
}
