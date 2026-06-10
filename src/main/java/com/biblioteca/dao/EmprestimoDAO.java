package com.biblioteca.dao;

import com.biblioteca.model.Emprestimo;
import com.biblioteca.model.RelatorioDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EmprestimoDAO {

    public boolean realizarEmprestimo(Emprestimo emprestimo) {
        String sqlEmprestimo = "INSERT INTO emprestimos (id_livro, id_usuario, data_saida, data_prevista) VALUES (?, ?, ?, ?)";
        String sqlUpdateLivro = "UPDATE livros SET disponivel = 0 WHERE id = ?";
        String sqlBloquearUsuario = "UPDATE usuarios SET bloqueado = 1 WHERE id = ?";

        Connection conn = null;
        try {
            conn = Conexao.connect();
            conn.setAutoCommit(false);
            try (PreparedStatement pstmtEmp = conn.prepareStatement(sqlEmprestimo)) {
                pstmtEmp.setInt(1, emprestimo.getIdLivro());
                pstmtEmp.setInt(2, emprestimo.getIdUsuario());
                pstmtEmp.setString(3, emprestimo.getDataEmprestimo().toString());
                pstmtEmp.setString(4, emprestimo.getDataDevolucaoPrevista().toString());
                pstmtEmp.executeUpdate();
            }
            try (PreparedStatement pstmtLivro = conn.prepareStatement(sqlUpdateLivro)) {
                pstmtLivro.setInt(1, emprestimo.getIdLivro());
                pstmtLivro.executeUpdate();
            }

            try (PreparedStatement pstmtUser = conn.prepareStatement(sqlBloquearUsuario)) {
                pstmtUser.setInt(1, emprestimo.getIdUsuario());
                pstmtUser.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            System.err.println("Erro ao realizar empréstimo: " + e.getMessage());
            return false;
        }
    }

    public void registrarDevolucao(int idEmprestimo, int idLivro) {
        String sqlDevolucao = "UPDATE emprestimos SET data_devolucao_real = ? WHERE id = ?";
        String sqlLivroDisponivel = "UPDATE livros SET disponivel = 1 WHERE id = ?";
        String sqlLiberarUsuario = "UPDATE usuarios SET bloqueado = 0 WHERE id = (SELECT id_usuario FROM emprestimos WHERE id = ?)";

        try (Connection conn = Conexao.connect()) {
            conn.setAutoCommit(false);

            try (PreparedStatement pstmtDev = conn.prepareStatement(sqlDevolucao)) {
                pstmtDev.setString(1, LocalDate.now().toString());
                pstmtDev.setInt(2, idEmprestimo);
                pstmtDev.executeUpdate();
            }

            try (PreparedStatement pstmtLiv = conn.prepareStatement(sqlLivroDisponivel)) {
                pstmtLiv.setInt(1, idLivro);
                pstmtLiv.executeUpdate();
            }

            try (PreparedStatement pstmtUsr = conn.prepareStatement(sqlLiberarUsuario)) {
                pstmtUsr.setInt(1, idEmprestimo);
                pstmtUsr.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            System.err.println("Erro na devolução: " + e.getMessage());
        }
    }

    public int buscarIdEmprestimoAberto(int idLivro) {
        String sql = "SELECT id FROM emprestimos WHERE id_livro = ? AND data_devolucao_real IS NULL";
        try (Connection conn = Conexao.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idLivro);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt("id");
        } catch (SQLException e) { e.printStackTrace(); }
        return -1;
    }

    public List<RelatorioDTO> listarRelatorio() {
        List<RelatorioDTO> lista = new ArrayList<>();
        String sql = "SELECT l.codigo, u.nome, l.titulo, e.data_saida, e.data_prevista " +
                "FROM emprestimos e " +
                "JOIN livros l ON e.id_livro = l.id " +
                "JOIN usuarios u ON e.id_usuario = u.id " +
                "WHERE e.data_devolucao_real IS NULL " +
                "ORDER BY e.data_saida ASC";

        try (Connection conn = Conexao.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                RelatorioDTO dto = new RelatorioDTO();
                dto.setCodigoLivro(rs.getString("codigo"));
                dto.setNomeUsuario(rs.getString("nome"));
                dto.setTituloLivro(rs.getString("titulo"));
                dto.setDataEmprestimo(LocalDate.parse(rs.getString("data_saida")));
                dto.setDataPrevista(LocalDate.parse(rs.getString("data_prevista")));
                if (LocalDate.now().isAfter(dto.getDataPrevista())) {
                    dto.setStatus("Atrasado");
                } else {
                    dto.setStatus("No Prazo");
                }
                lista.add(dto);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    }

    public boolean estenderPrazoEmprestimo(String codigoLivro, int diasAdicionais) {
        String sql = "UPDATE emprestimos SET data_prevista = date(data_prevista, '+' || ? || ' days') " +
                "WHERE data_devolucao_real IS NULL AND id_livro = (SELECT id FROM livros WHERE codigo = ?)";

        try (Connection conn = Conexao.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, diasAdicionais);
            pstmt.setString(2, codigoLivro);

            int linhasAfetadas = pstmt.executeUpdate();
            return linhasAfetadas > 0;
        } catch (SQLException e) {
            System.err.println("Erro ao estender prazo: " + e.getMessage());
            return false;
        }
    }
}