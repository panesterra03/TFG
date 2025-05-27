package com.gestionHoteles.TFG.Repository;



import org.springframework.data.jpa.repository.JpaRepository;
import com.gestionHoteles.TFG.Entity.Hotel;



public interface RepositoryHotel extends JpaRepository<Hotel, Long> {
    Hotel findByNombre(String nombre);

    
}
