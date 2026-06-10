package com.biblioteca.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class RelatorioDTO {
    private String codigoLivro;
    private String nomeUsuario;
    private String tituloLivro;
    private LocalDate dataEmprestimo;
    private LocalDate dataPrevista;
    private String status;

    public RelatorioDTO() {}

    public RelatorioDTO(String codigoLivro, String nomeUsuario, String tituloLivro,
                        LocalDate dataEmprestimo, LocalDate dataPrevista) {
        this.codigoLivro = codigoLivro;
        this.nomeUsuario = nomeUsuario;
        this.tituloLivro = tituloLivro;
        this.dataEmprestimo = dataEmprestimo;
        this.dataPrevista = dataPrevista;
        this.status = definirStatus();
    }

    public String definirStatus() {
        if (dataPrevista == null) return "Indefinido";

        if (LocalDate.now().isAfter(dataPrevista)) {
            long dias = ChronoUnit.DAYS.between(dataPrevista, LocalDate.now());
            return "Atrasado (" + dias + " dias)";
        }
        return "No Prazo";
    }

    public String getCodigoLivro() { return codigoLivro; }
    public void setCodigoLivro(String codigoLivro) { this.codigoLivro = codigoLivro; }

    public String getNomeUsuario() { return nomeUsuario; }
    public void setNomeUsuario(String nomeUsuario) { this.nomeUsuario = nomeUsuario; }

    public String getTituloLivro() { return tituloLivro; }
    public void setTituloLivro(String tituloLivro) { this.tituloLivro = tituloLivro; }

    public LocalDate getDataEmprestimo() { return dataEmprestimo; }
    public void setDataEmprestimo(LocalDate dataEmprestimo) { this.dataEmprestimo = dataEmprestimo; }

    public LocalDate getDataPrevista() { return dataPrevista; }
    public void setDataPrevista(LocalDate dataPrevista) {
        this.dataPrevista = dataPrevista;
        this.status = definirStatus();
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}