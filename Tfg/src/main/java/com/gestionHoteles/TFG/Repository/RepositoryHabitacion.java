package com.gestionHoteles.TFG.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.gestionHoteles.TFG.Entity.Habitacion;
import com.gestionHoteles.TFG.Entity.Hotel;

import java.util.List;

@Repository
public interface RepositoryHabitacion extends JpaRepository<Habitacion, Long> {
    List<Habitacion> findByHotel(Hotel hotel);
    Habitacion findByNuemro(String numero);
Habitacion findByHotelAndNuemro(Hotel hotel, String numero);

}
