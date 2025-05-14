package com.gestionHoteles.TFG.Controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

import com.gestionHoteles.TFG.Entity.Habitacion;
import com.gestionHoteles.TFG.Entity.Hotel;
import com.gestionHoteles.TFG.Entity.Reserva;
import com.gestionHoteles.TFG.Repository.RepositoryHabitacion;
import com.gestionHoteles.TFG.Repository.RepositoryHotel;
import com.gestionHoteles.TFG.Services.ServiceReserva;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
import java.security.Provider.Service;
import java.util.ArrayList;
import java.util.HashMap;


@RestController
@RequestMapping("/api/private/hotel") 

public class HotelesController {
 
    @Autowired
    private RepositoryHotel repositoryHotel;

    @Autowired
    private RepositoryHabitacion repositoryHabitacion;
    
    @PostMapping("/crearHotel") //crear un hotel quien lo diria
    public ResponseEntity<?> createHotel(@RequestBody Map<String, String> data) {
        try {

            String nombre = data.get("nombre");
            String direccion = data.get("direccion");

            Hotel hotel = repositoryHotel.findByNombre(nombre);          
            if (hotel != null) {//mandar badRequest con mensaje de que ya existe el hotel 
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "el hotel ya existe");
                return ResponseEntity.badRequest().body(response);
            }else{
                // Verificar que el nombre y la dirección no sean nulos o vacíos
                if (nombre == null || nombre.isEmpty()) {
                    throw new RuntimeException("El nombre del hotel es requerido");
                }
                if (direccion == null || direccion.isEmpty()) {
                    throw new RuntimeException("La ubicación del hotel es requerida");
                }
                

            }

            Hotel nuevoHotel = new Hotel();
            nuevoHotel.setNombre(nombre);
            nuevoHotel.setDireccion(direccion);


            // Guardar el nuevo el hotel en la base de datos pero aun no esta implementado
            Hotel savedHotel = repositoryHotel.save(nuevoHotel);
            return ResponseEntity.ok(savedHotel);
                    

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/crearHabitacion")
    public ResponseEntity<?> createHabitacion(@RequestBody Map<String, String> data) {
        try {
            String numero = data.get("numero");
            String planta = data.get("planta");
            String nMaxStr = data.get("nmax");
            Long hotelId = Long.parseLong(data.get("hotelId"));

            // Validar que los datos no sean nulos o vacíos
            if (numero == null || numero.isEmpty()) {
                throw new RuntimeException("El número de la habitación es requerido");
            }
            if (planta == null || planta.isEmpty()) {
                throw new RuntimeException("La planta de la habitación es requerida");
            }
            if (nMaxStr == null || nMaxStr.isEmpty()) {
                throw new RuntimeException("El número máximo de personas es requerido");
            }

            int nMax = Integer.parseInt(nMaxStr);
            if (nMax <= 0) {
                throw new RuntimeException("El número máximo de personas debe ser mayor que 0");
            }

            // Buscar el hotel por ID
            Hotel hotel = repositoryHotel.findById(hotelId).orElse(null);
            if (hotel == null) {
                return ResponseEntity.badRequest().body("El hotel no existe");
            }

            // Crear la nueva habitación y asociarla al hotel
            Habitacion nuevaHabitacion = new Habitacion();
            nuevaHabitacion.setNuemro(numero);
            nuevaHabitacion.setHotel(hotel);
            nuevaHabitacion.setPlanta(planta);
            nuevaHabitacion.setNmax(nMax);

            // Añadir la habitación al hotel y guardar
            hotel.getHabitaciones().add(nuevaHabitacion);
            repositoryHotel.save(hotel);

            return ResponseEntity.ok(nuevaHabitacion);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/buscarHabitacion")
    public ResponseEntity<?> findByNumero(@RequestParam("numero") String numero) {
        try {
            Habitacion habitacion = repositoryHabitacion.findByNuemro(numero);
            if (habitacion != null) {
                Map<String, Object> response = new HashMap<>();
                response.put("numero", habitacion.getNuemro());
                response.put("planta", habitacion.getPlanta());
                response.put("nmax", habitacion.getNmax());
                response.put("hotel", habitacion.getHotel().getNombre());
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/listarHoteles")
    public ResponseEntity<?> listarHoteles() {
        try {
            List<String> hotelesNombre = new ArrayList<>(); 
            for (Hotel hotel : repositoryHotel.findAll()) {
                hotelesNombre.add(hotel.getNombre());
            }

            return ResponseEntity.ok(hotelesNombre);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @GetMapping("/listarHabitaciones")
    public ResponseEntity<?> listarHabitaciones(@RequestParam("hotel") String hotelt) {
        try {
            // Obtener las habitaciones asociadas al hotel
            Hotel hotel = repositoryHotel.findByNombre(hotelt);
            List<Habitacion> habitaciones = repositoryHabitacion.findByHotel(hotel);
            List<Map<String, Object>> habitacionesInfo = new ArrayList<>(); 

            for (Habitacion habitacion : habitaciones) {
                Map<String, Object> info = new HashMap<>();
                info.put("numero", habitacion.getNuemro());
                info.put("planta", habitacion.getPlanta());
                info.put("nmax", habitacion.getNmax());
                habitacionesInfo.add(info);
            }
            return ResponseEntity.ok(habitacionesInfo);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    
}

