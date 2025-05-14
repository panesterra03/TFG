package com.gestionHoteles.TFG.Entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;


@Entity
@Table(name = "hoteles")
public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_hotel")
    private Long id;

    @Column(name = "nombre", nullable = false, unique = true)
    private String nombre;

    @Column(name = "direccion", nullable = false, unique = true)
    private String direccion;

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Habitacion> habitaciones = new ArrayList<>();

    public Hotel() {
        
        }

    public Hotel(Long id, String nombre, String direccion, List<Habitacion> habitaciones) {
        this.id = id;
        this.nombre = nombre;
        this.direccion = direccion;
        this.habitaciones = habitaciones;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public List<Habitacion> getHabitaciones() {
        return habitaciones;
    }

    public void setHabitaciones(List<Habitacion> habitaciones) {
        this.habitaciones = habitaciones;
    }
    
}
