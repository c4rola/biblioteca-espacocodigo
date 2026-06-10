package com.biblioteca;

import com.biblioteca.dao.Conexao;

public class Main {
    public static void main(String[] args) {
        Conexao.criarTabelasIniciais();

        BibliotecaApp.main(args);
    }
}