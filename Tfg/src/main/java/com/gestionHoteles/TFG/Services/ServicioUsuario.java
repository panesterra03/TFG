package com.gestionHoteles.TFG.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gestionHoteles.TFG.Entity.Usuario;
import com.gestionHoteles.TFG.Repository.RepositoryUsuario;

@Service
public class ServicioUsuario {

     @Autowired
    private RepositoryUsuario usuarios;


     public Usuario inicioSesion(String correo, String contraseña) {
      
        if (correo == null || correo.isEmpty()) {  // Verificar que el correo no sea nulo
            throw new RuntimeException("El correo es requerido");
        }
        
      
        Usuario login = usuarios.findByCorreo(correo);  // Buscar el usuario por correo
        
        if(login==null){

        login = usuarios.findByNombre(correo); // Si el correo no funcio busca en nopmbre usuario

        }



        // Verificar que el usuario exista
        if (login == null) {
            return null; // Devuelve directamente null
        }
        
        // Verificar la contraseña
        if (contraseña != null && contraseña.equals(login.getContraseña())) {
            return login; // Credenciales correctas
        } else {
            return null; // Si la contraseña no existe , o no es la correcta devuelve null
        }
    }
    
    public Usuario registrarUsuario(String nombre, String correo, String contraseña) {


     // Validar datos
     if (nombre == null || nombre.isEmpty()) {
        throw new RuntimeException("El nombre es requerido");
    }
    
    if (correo == null || correo.isEmpty()) {
        throw new RuntimeException("El correo es requerido");
    }
    
    if (contraseña == null || contraseña.isEmpty()) {
        throw new RuntimeException("La contraseña es requerida");
    }
    
    // Verificar si el correo ya está registrado
    Usuario usuarioExistente = usuarios.findByCorreo(correo);
    if (usuarioExistente != null) {
        throw new RuntimeException("El correo ya está registrado");
    }
 
    Usuario nuevoUsuario = new Usuario();
    nuevoUsuario.setNombre(nombre);
    nuevoUsuario.setCorreo(correo);
    nuevoUsuario.setContraseña(contraseña); 
    nuevoUsuario.setRol("user");


    // Guardar el nuevo usuario en la base de datos
    return usuarios.save(nuevoUsuario);
    }
}