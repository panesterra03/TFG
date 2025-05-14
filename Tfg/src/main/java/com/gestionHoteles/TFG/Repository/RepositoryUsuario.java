package com.gestionHoteles.TFG.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.gestionHoteles.TFG.Entity.Usuario;

public interface RepositoryUsuario extends JpaRepository<Usuario, Long> {
    Usuario findByCorreo(String correo);
    Usuario findByNombre(String nombre);
}
