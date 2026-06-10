package com.biblioteca.dao;

import com.biblioteca.model.Livro;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LivroDAO {

    public boolean salvar(Livro livro) {
        String sql = "INSERT INTO livros (codigo, titulo, autor, disponivel) VALUES (?, ?, ?, ?)";

        try (Connection conn = Conexao.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, livro.getCodigo());
            pstmt.setString(2, livro.getTitulo());
            pstmt.setString(3, livro.getAutor());
            pstmt.setBoolean(4, livro.isDisponivel());

            pstmt.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Erro técnico ao salvar livro: " + e.getMessage());
            return false;
        }
    }
    public List<Livro> buscarTodos() {
        List<Livro> livros = new ArrayList<>();
        String sql = "SELECT * FROM livros";

        try (Connection conn = Conexao.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Livro livro = new Livro(
                        rs.getInt("id"),
                        rs.getString("codigo"),
                        rs.getString("titulo"),
                        rs.getString("autor"),
                        rs.getBoolean("disponivel")
                );
                livros.add(livro);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar livros: " + e.getMessage());
        }
        return livros;
    }
    public void excluir(int id) {
        String sql = "DELETE FROM livros WHERE id = ?";

        try (Connection conn = Conexao.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Erro ao excluir livro: " + e.getMessage());
        }
    }

    public List<Livro> buscarComFiltros(String titulo, String autor, String codigo, String status) {
        List<Livro> livros = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM livros WHERE 1=1");
        String removerAcentos = "REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(LOWER(%s), 'á', 'a'), 'é', 'e'), 'í', 'i'), 'ó', 'o'), 'ú', 'u')";

        if (titulo != null && !titulo.trim().isEmpty()) {
            sql.append(" AND " + String.format(removerAcentos, "titulo") + " LIKE " + String.format(removerAcentos, "?"));
        }
        if (autor != null && !autor.trim().isEmpty()) {
            sql.append(" AND " + String.format(removerAcentos, "autor") + " LIKE " + String.format(removerAcentos, "?"));
        }
        if (codigo != null && !codigo.trim().isEmpty()) {
            sql.append(" AND codigo LIKE ?");
        }

        if ("Disponível".equals(status)) sql.append(" AND disponivel = 1");
        else if ("Indisponível".equals(status)) sql.append(" AND disponivel = 0");

        try (Connection conn = Conexao.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;

            if (titulo != null && !titulo.trim().isEmpty())
                pstmt.setString(paramIndex++, "%" + titulo.trim().toLowerCase() + "%");

            if (autor != null && !autor.trim().isEmpty())
                pstmt.setString(paramIndex++, "%" + autor.trim().toLowerCase() + "%");

            if (codigo != null && !codigo.trim().isEmpty())
                pstmt.setString(paramIndex++, codigo.trim() + "%");

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                livros.add(new Livro(rs.getInt("id"), rs.getString("codigo"),
                        rs.getString("titulo"), rs.getString("autor"), rs.getBoolean("disponivel")));
            }
        } catch (SQLException e) {
            System.err.println("Erro na busca: " + e.getMessage());
        }
        return livros;
    }
}
