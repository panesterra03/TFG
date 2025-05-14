package com.gestionHoteles.TFG.utils;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;


    
    import java.security.Key;
    
    public class JwtUtil {
    
        // Genera una clave segura desde una cadena secreta
        private static final String SECRET = "PatataUltraMegaSecreta123456789qwertyuioplkjhgfdsazxcvbnm";
        private static final Key SECRET_KEY = Keys.hmacShaKeyFor(SECRET.getBytes());
    
        public String generarToken(String userId) {
            return Jwts.builder()
                    .setSubject(userId)
                    .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                    .compact();
        }
    
        public boolean validateToken(String token) {
            try {
                Jwts.parserBuilder()
                        .setSigningKey(SECRET_KEY)
                        .build()
                        .parseClaimsJws(token);
                return true; // Token válido
            } catch (Exception e) {
                System.out.println("Token inválido: " + e.getMessage());
                return false;
            }
        }

    }
    