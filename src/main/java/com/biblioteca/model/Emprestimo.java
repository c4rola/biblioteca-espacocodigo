package com.biblioteca.model;

import java.time.LocalDate;

public class Emprestimo {
    private int id;
    private int idLivro;
    private int idUsuario;
    private LocalDate dataEmprestimo;
    private LocalDate dataDevolucaoPrevista;
    private LocalDate dataDevolucaoReal;

    public Emprestimo(int idLivro, int idUsuario, int diasPrazo) {
        this.idLivro = idLivro;
        this.idUsuario = idUsuario;
        this.dataEmprestimo = LocalDate.now();
        this.dataDevolucaoPrevista = dataEmprestimo.plusDays(diasPrazo);
    }

    public int getId() { return id; }
    public int getIdLivro() { return idLivro; }
    public int getIdUsuario() { return idUsuario; }
    public LocalDate getDataEmprestimo() { return dataEmprestimo; }
    public LocalDate getDataDevolucaoPrevista() { return dataDevolucaoPrevista; }
    public LocalDate getDataDevolucaoReal() { return dataDevolucaoReal; }
    public void setDataDevolucaoReal(LocalDate dataDevolucaoReal) { this.dataDevolucaoReal = dataDevolucaoReal; }
}