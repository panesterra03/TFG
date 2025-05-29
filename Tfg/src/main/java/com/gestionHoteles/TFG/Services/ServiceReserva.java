package com.gestionHoteles.TFG.Services;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gestionHoteles.TFG.Entity.Reserva;
import com.gestionHoteles.TFG.Repository.RepositoryReserva;
import com.gestionHoteles.TFG.Repository.RepositoryUsuario;
import com.gestionHoteles.TFG.Repository.RepositoryHabitacion;
import com.gestionHoteles.TFG.Entity.Usuario;
import com.gestionHoteles.TFG.Entity.Habitacion;
import com.gestionHoteles.TFG.Entity.Hotel;

@Service
public class ServiceReserva {

    @Autowired
    private RepositoryReserva reservaRepository;

    @Autowired
    private RepositoryUsuario usuarioRepository;

    @Autowired
    private RepositoryHabitacion habitacionRepository;

    public Reserva crearReserva(LocalDate fechaInicio, LocalDate fechaFin, int cantidad, Long iduser, String numeroHabitacion ,Hotel hotel) {
        LocalDate fechaActual = LocalDate.now();
        if (fechaInicio.isBefore(fechaActual) || fechaFin.isBefore(fechaActual)) {
            throw new RuntimeException("La fecha de inicio y fin deben ser posteriores a la fecha actual");
        }
        if (fechaInicio == null || fechaFin == null) {
            throw new RuntimeException("Las fechas son requeridas");
        }
        if (cantidad <= 0) {
            throw new RuntimeException("La cantidad debe ser mayor a cero");
        }
        if (iduser == null || iduser <= 0) {
            throw new RuntimeException("El nombre del usuario es requerido");
        }

        // Buscar el usuario por nombre
        Usuario usuarioEntity = usuarioRepository.findById(iduser).orElse(null);
        if (usuarioEntity == null) {
            throw new RuntimeException("Usuario no encontrado");
        }

    Habitacion habitacionEntity = habitacionRepository.findByHotelAndNuemro(hotel, numeroHabitacion);


        // Buscar la habitación por número
        if (habitacionEntity == null) {
            throw new RuntimeException("Habitación no encontrada");
        }


        // Verificar que la cantidad no exceda el máximo de la habitación
        if (cantidad > habitacionEntity.getNmax()) {
            throw new RuntimeException("La cantidad de personas excede el máximo permitido para esta habitación");
        }

        


        habitacionEntity.setEstado(false); // Cambiar el estado de la habitación a ocupada

        Reserva reserva = new Reserva();
        reserva.setFechaInicio(fechaInicio);
        reserva.setFechaFin(fechaFin);
        reserva.setCantidad(cantidad);
        reserva.setUsuario(usuarioEntity);
        reserva.setHabitacion(habitacionEntity);

        return reservaRepository.save(reserva);

    }

    public List<Reserva> listarReservas(Long idUsuario) {
  
        List<Reserva> reservas = reservaRepository.findByUsuarioId(idUsuario);
        if (reservas.isEmpty()) {
            throw new RuntimeException("No hay reservas para este usuario");
        }
        return reservas;
    }

    
    public void borrarReserva(Long idReserva) {
        Reserva reserva = reservaRepository.findById(idReserva).orElse(null);
        if (reserva == null) {
            throw new RuntimeException("Reserva no encontrada");
        }
        Habitacion habitacion =reserva.getHabitacion();
        habitacion.setEstado(true);
        habitacionRepository.save(habitacion);
        reservaRepository.delete(reserva);
    }

    public List<Reserva> todo() {
return reservaRepository.findAll();
    }

}