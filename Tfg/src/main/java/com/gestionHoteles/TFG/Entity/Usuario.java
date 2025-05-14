package com.gestionHoteles.TFG.Entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name ="usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_usuario")
    private Long id;    

    @Column(name = "usuario", nullable = false, unique = true)
    private String nombre; 

    @Column(name = "correo", nullable = false, unique = true)
    private String correo;

    @Column(name = "contraseña", nullable = false)
    private String contraseña;

    @Column(name = "rol")
    private String rol;

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Reserva> reservas = new ArrayList<>();

    public Usuario() {
        // Constructor vacío requerido por JPA
    }

    public Usuario(Long id, String nombre, String correo, String contraseña, String rol, List<Reserva> reservas) {
        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
        this.contraseña = contraseña;
        this.rol = rol;
        this.reservas = reservas;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getContraseña() {
        return contraseña;
    }

    public void setContraseña(String contraseña) {
        this.contraseña = contraseña;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }    
   
    // Getters y setters para reservas
    public List<Reserva> getReservas() {
        return reservas;
    }

    public void setReservas(List<Reserva> reservas) {
        this.reservas = reservas;
    }

    //para manejar  reservas y usuarios
    public void addReserva(Reserva reserva) {
        reservas.add(reserva);
        reserva.setUsuario(this);
    }

    public void removeReserva(Reserva reserva) {
        reservas.remove(reserva);
        reserva.setUsuario(null);
    }



    public Long getId() {
        return id;
    }
}