
package com.example.studydesk.backend.model;

import jakarta.persistence.*;
import java.util.Locale;

@Entity
@Table(name = "entregas")
public class Entrega {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String materia;
    private String titulo;
    private String fecha;

    // Campo anterior: compatibilidad
    private Integer usuarioId;

    // Correo de la cuenta propietaria
    @Column(name = "correo_usuario", length = 254)
    private String correoUsuario;

    // NUEVO: identificador unico de sincronizacion
    // Puede ser null para las entregas antiguas
    @Column(
            name = "id_cliente",
            length = 100,
            unique = true
    )
    private String idCliente;

    public Entrega() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMateria() {
        return materia;
    }

    public void setMateria(String materia) {
        this.materia = materia;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public Integer getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Integer usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getCorreoUsuario() {
        return correoUsuario;
    }

    public void setCorreoUsuario(String correoUsuario) {
        this.correoUsuario = correoUsuario == null
                ? null
                : correoUsuario.trim().toLowerCase(Locale.ROOT);
    }

    public String getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(String idCliente) {
        this.idCliente = idCliente == null
                ? null
                : idCliente.trim();
    }
}
