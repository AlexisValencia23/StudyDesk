
package com.example.studydesk.backend.model;

import jakarta.persistence.*;
import java.util.Locale;

@Entity
@Table(name = "apuntes")
public class Apunte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String materia;
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String contenido;

    private String fecha;

    // Campo anterior: se conserva por compatibilidad
    private Integer usuarioId;

    // Nuevo campo para asociar el apunte al correo
    @Column(name = "correo_usuario", length = 254)
    private String correoUsuario;

    public Apunte() {
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

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
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
}
