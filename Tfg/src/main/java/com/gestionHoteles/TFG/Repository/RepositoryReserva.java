package com.gestionHoteles.TFG.Repository;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gestionHoteles.TFG.Entity.Reserva;


public interface RepositoryReserva extends JpaRepository<Reserva, Long> {
    List<Reserva> findByUsuarioId(Long usuarioId);
}
