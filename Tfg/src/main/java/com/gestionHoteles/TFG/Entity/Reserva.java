package com.gestionHoteles.TFG.Entity;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;

@Entity
@Table(name ="Reservas")
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_reservas")
    private Long id; 

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Column(name = "cantidad", nullable = false)
    private int cantidad;

 

    @ManyToOne
    @JoinColumn(name = "ID_usuario", nullable = false)
    @JsonIgnore
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "ID_habitacion", nullable = false)
    private Habitacion habitacion;

    public Reserva() {
    }

    public Reserva(LocalDate fechaInicio, LocalDate fechaFin, int cantidad, Usuario usuario, Habitacion habitacion) {
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.cantidad = cantidad;
        this.usuario = usuario;
        this.habitacion = habitacion;
    }

    public Long getId() {
        return id;
    }
    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Habitacion getHabitacion() {
        return habitacion;
    }

    public void setHabitacion(Habitacion habitacion) {
        this.habitacion = habitacion;
    }
   
    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

}
