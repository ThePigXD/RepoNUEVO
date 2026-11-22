package com.example.listas;

public class Videojuego {
    private String titulo;
    private String genero;
    private String plataforma;
    private double calificacion;
    private int anioLanzamiento;

    public Videojuego(String titulo, String genero, String plataforma, double calificacion, int anioLanzamiento) {
        this.titulo = titulo;
        this.genero = genero;
        this.plataforma = plataforma;
        this.calificacion = calificacion;
        this.anioLanzamiento = anioLanzamiento;
    }

    public Videojuego() {
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public String getPlataforma() {
        return plataforma;
    }

    public void setPlataforma(String plataforma) {
        this.plataforma = plataforma;
    }

    public double getCalificacion() {
        return calificacion;
    }

    public void setCalificacion(double calificacion) {
        this.calificacion = calificacion;
    }

    public int getAnioLanzamiento() {
        return anioLanzamiento;
    }

    public void setAnioLanzamiento(int anioLanzamiento) {
        this.anioLanzamiento = anioLanzamiento;
    }
}
