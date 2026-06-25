package com.biblioteca.model;

public class Livro {
    private int id;
    private String codigo;
    private String titulo;
    private String autor;
    private boolean disponivel;

    public Livro(int id, String codigo, String titulo, String autor, boolean disponivel) {
        this.id = id;
        this.codigo = codigo;
        this.titulo = titulo;
        this.autor = autor;
        this.disponivel = disponivel;
    }

    public Livro(String codigo, String titulo, String autor) {
        this.codigo = codigo;
        this.titulo = titulo;
        this.autor = autor;
        this.disponivel = true;
    }

    public int getId() { return id; }
    public String getCodigo() { return codigo; }
    public String getTitulo() { return titulo; }
    public String getAutor() { return autor; }
    public void setTitulo(String titulo) {this.titulo = titulo;}
    public void setAutor(String autor) {this.autor = autor;}
    public void setCodigo(String codigo) {this.codigo = codigo;}

    public boolean isDisponivel() { return disponivel; }
    public void setDisponivel(boolean disponivel) { this.disponivel = disponivel; }
}
