package com.biblioteca.dao;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Conexao {

    private static String getDBPath() {
        String userHome = System.getProperty("user.home");
        String appFolder = new File(userHome, "BibliotecaMarcusFélix").getAbsolutePath();

        File dir = new File(appFolder);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                System.err.println("Erro: não foi possível criar a pasta " + appFolder);
            }
        }

        String dbPath = new File(dir, "biblioteca.db").getAbsolutePath();
        return "jdbc:sqlite:" + dbPath;
    }

    public static Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(getDBPath());
        } catch (SQLException e) {
            System.err.println("Erro ao conectar ao SQLite: " + e.getMessage());
        }
        return conn;
    }
    public static void criarTabelasIniciais() {
        String sqlLivros = """
        CREATE TABLE IF NOT EXISTS livros (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            codigo TEXT NOT NULL UNIQUE,
            titulo TEXT NOT NULL,
            autor TEXT,
            disponivel BOOLEAN DEFAULT 1
        );
    """;

        String sqlUsuarios = """
        CREATE TABLE IF NOT EXISTS usuarios (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            nome TEXT NOT NULL,
            cpf TEXT NOT NULL UNIQUE,
            email TEXT,
            telefone TEXT,
            bloqueado BOOLEAN DEFAULT 0
        );
    """;

        String sqlEmprestimos = """
        CREATE TABLE IF NOT EXISTS emprestimos (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            id_livro INTEGER NOT NULL,
            id_usuario INTEGER NOT NULL,
            data_saida DATE NOT NULL,
            data_prevista DATE NOT NULL,
            data_devolucao_real DATE,
            FOREIGN KEY (id_livro) REFERENCES livros(id),
            FOREIGN KEY (id_usuario) REFERENCES usuarios(id)
        );
    """;

        String sqlConfiguracoes = """
        CREATE TABLE IF NOT EXISTS configuracoes (
                id INTEGER PRIMARY KEY CHECK (id = 1),
                email_remetente TEXT,
                senha_app TEXT
        );
    """;

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sqlLivros);
            stmt.execute(sqlUsuarios);
            stmt.execute(sqlEmprestimos);
            stmt.execute(sqlConfiguracoes);
            System.out.println("Tabelas verificadas/criadas com sucesso.");
        } catch (SQLException e) {
            System.err.println("Erro ao criar tabelas: " + e.getMessage());
        }
    }
}