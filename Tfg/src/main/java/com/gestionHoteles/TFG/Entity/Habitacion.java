package com.gestionHoteles.TFG.Entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name ="habitaciones")
public class Habitacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_habitacion")
    private Long id;    

    @Column(name = "numero", nullable = false, unique = true)
    private String nuemro; 

    @Column(name = "planta", nullable = false)
    private String planta;

    @Column(name = "Nmax", nullable = false)//numero maximo de personas por habitacion
    private int Nmax;


    @OneToMany(mappedBy = "habitacion", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Reserva> reservas = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "ID_hotel", nullable = false)
    private Hotel hotel;

    public Habitacion() {
        // Constructor vacío requerido por JPA
    }

    public Habitacion(Long id, String nuemro, String planta, List<Reserva> reservas, Hotel hotel) {
        this.id = id;
        this.nuemro = nuemro;
        this.planta = planta;
        this.reservas = reservas;
        this.hotel = hotel;
    }

    public Long getId() {
        return id;
    }

    public String getNuemro() {
        return nuemro;
    }

    public void setNuemro(String nuemro) {
        this.nuemro = nuemro;
    }

    public String getPlanta() {
        return planta;
    }

    public void setPlanta(String planta) {
        this.planta = planta;
    }

    public List<Reserva> getReservas() {
        return reservas;
    }

    public void setReservas(List<Reserva> reservas) {
        this.reservas = reservas;
    }

    public Hotel getHotel() {
        return hotel;
    }

    public void setHotel(Hotel hotel) {
        this.hotel = hotel;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getNmax() {
        return Nmax;
    }

    public void setNmax(int nmax) {
        Nmax = nmax;
    }
}
