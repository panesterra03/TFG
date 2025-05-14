package com.gestionHoteles.TFG.Controller;

import org.springframework.beans.factory.annotation.Autowired;

import com.gestionHoteles.TFG.Entity.Usuario;
import com.gestionHoteles.TFG.Services.ServicioUsuario;
import com.gestionHoteles.TFG.utils.JwtUtil;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.HashMap;


@RestController
@RequestMapping("/usuario") 

public class ControladorUsuario {
    @Autowired
    private ServicioUsuario servicio;  

    @PostMapping("/inicioSesion") 
    public ResponseEntity<?> iniciarSesion(@RequestBody Map<String, String> credenciales) {
        try {
            String correo = credenciales.get("correo");
            String contraseña = credenciales.get("contraseña");
            
            if (correo == null || contraseña == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Correo y contraseña son requeridos");
                return ResponseEntity.badRequest().body(response);
            }
            JwtUtil jwtUtil = new JwtUtil();
           
            Usuario usuario = servicio.inicioSesion(correo, contraseña);   


             if (usuario != null) {
                String idUsuario = String.valueOf(usuario.getId()); // adaptamos el id para que se pueda mandar correctamente por el generador de token
                String token = jwtUtil.generarToken(idUsuario); // Genera el JWT
    
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "Login exitoso");
                response.put("rol", usuario.getRol());
                response.put("userId", usuario.getId());
                response.put("token", token); 
                return ResponseEntity.ok(response);
             }
            else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Credenciales incorrectas");
                return ResponseEntity.ok(response);
            }

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/registro")//mejorar con webtoken cuando aprenda
    public ResponseEntity<?> registrarUsuario(@RequestBody Map<String, String> datos) {
        try {
            String nombre = datos.get("nombre");
            String correo = datos.get("correo");
            String contraseña = datos.get("contraseña");
            
            if (nombre == null || correo == null || contraseña == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Todos los campos son requeridos");
                return ResponseEntity.badRequest().body(response);
            }
            
            Usuario usuario = servicio.registrarUsuario(nombre, correo, contraseña);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Usuario registrado correctamente");
            response.put("usuario", usuario);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
