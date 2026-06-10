package com.biblioteca.dao;

import com.biblioteca.model.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    public boolean salvar(Usuario usuario) {
        String sql = "INSERT INTO usuarios (nome, cpf, email, telefone, bloqueado) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = Conexao.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, usuario.getNome());
            pstmt.setString(2, usuario.getCpf());
            pstmt.setString(3, usuario.getEmail());
            pstmt.setString(4, usuario.getTelefone());
            pstmt.setBoolean(5, usuario.isBloqueado());

            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Erro ao salvar usuário: " + e.getMessage());
            return false;
        }
    }

    public boolean atualizar(Usuario usuario) {
        String sql = "UPDATE usuarios SET nome = ?, cpf = ?, email = ?, telefone = ?, bloqueado = ? WHERE id = ?";

        try (Connection conn = Conexao.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, usuario.getNome());
            pstmt.setString(2, usuario.getCpf());
            pstmt.setString(3, usuario.getEmail());
            pstmt.setString(4, usuario.getTelefone());
            pstmt.setBoolean(5, usuario.isBloqueado());
            pstmt.setInt(6, usuario.getId());

            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar usuário: " + e.getMessage());
            return false;
        }
    }

    public Usuario buscarPorCpf(String cpf) {
        String sql = "SELECT * FROM usuarios WHERE cpf = ?";

        try (Connection conn = Conexao.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, cpf);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Usuario u = new Usuario(rs.getString("nome"), rs.getString("cpf"),
                        rs.getString("email"), rs.getString("telefone"));
                u.setId(rs.getInt("id"));
                u.setBloqueado(rs.getBoolean("bloqueado"));
                return u;
            }
        } catch (SQLException e) {
            System.err.println("Erro ao buscar usuário: " + e.getMessage());
        }
        return null;
    }

    public void atualizarStatusBloqueio(String cpf, boolean status) {
        String sql = "UPDATE usuarios SET bloqueado = ? WHERE cpf = ?";

        try (Connection conn = Conexao.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBoolean(1, status);
            pstmt.setString(2, cpf);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erro ao atualizar status: " + e.getMessage());
        }
    }

    public boolean possuiEmprestimoAtivo(int idUsuario) {
        String sql = "SELECT COUNT(*) FROM emprestimos WHERE id_usuario = ? AND data_devolucao_real IS NULL";

        try (Connection conn = Conexao.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idUsuario);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Erro ao verificar empréstimo ativo: " + e.getMessage());
        }
        return false;
    }

    public List<Usuario> listarTodos() {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT * FROM usuarios ORDER BY nome ASC";

        try (Connection conn = Conexao.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Usuario usuario = new Usuario(
                        rs.getString("nome"),
                        rs.getString("cpf"),
                        rs.getString("email"),
                        rs.getString("telefone")
                );
                usuario.setId(rs.getInt("id"));
                usuario.setBloqueado(rs.getInt("bloqueado") == 1);

                lista.add(usuario);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar usuários: " + e.getMessage());
        }
        return lista;
    }

    public void excluir(int id) {
        String sql = "DELETE FROM usuarios WHERE id = ?";

        try (Connection conn = Conexao.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Erro ao excluir usuário: " + e.getMessage());
        }
    }
}