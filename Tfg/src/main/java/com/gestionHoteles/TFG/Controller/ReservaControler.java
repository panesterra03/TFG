package com.gestionHoteles.TFG.Controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.gestionHoteles.TFG.Entity.Reserva;
import com.gestionHoteles.TFG.Services.ServiceReserva;

@RestController
@RequestMapping("/api/private/reserva")
public class ReservaControler {
    @Autowired
    private ServiceReserva servicio;  
@PostMapping("/crearReserva")
public ResponseEntity<?> crearReserva(@RequestBody Map<String, Object> reservaData) {
    try {
        // Parsear y validar los datos
        String nombreUsuario = (String) reservaData.get("user_id"); 
        String numeroHabitacion = (String) reservaData.get("habitacion");
        String fechaInicioStr = (String) reservaData.get("fechaInicio");
        String fechaFinStr = (String) reservaData.get("fechaFin");
        int cantidad = reservaData.get("cantidad") != null ? Integer.parseInt(reservaData.get("cantidad").toString()) : 1;

        if (nombreUsuario == null || numeroHabitacion == null || fechaInicioStr == null || fechaFinStr == null) {
            return ResponseEntity.badRequest().body("Faltan datos obligatorios.");
        }

        Long id = Long.parseLong(nombreUsuario); 
        // Convertir fechas
        LocalDate fechaInicio = LocalDate.parse(fechaInicioStr);
        LocalDate fechaFin = LocalDate.parse(fechaFinStr);

        // Llamar al servicio
        Reserva nuevaReserva = servicio.crearReserva(fechaInicio, fechaFin, cantidad, id, numeroHabitacion);

        return ResponseEntity.ok(nuevaReserva);
    } catch (Exception e) {
        return ResponseEntity.badRequest().body("Error al crear la reserva: " + e.getMessage());
    }
}

    @GetMapping("/ListaReservas/{idUsuario}")
    public List<Reserva> ListaReservas(@PathVariable Long idUsuario) {
    return servicio.listarReservas(idUsuario);
}
}
