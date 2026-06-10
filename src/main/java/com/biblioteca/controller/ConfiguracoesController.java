package com.biblioteca.controller;

import com.biblioteca.dao.Conexao;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ConfiguracoesController {
    @FXML private TextField txtEmail;
    @FXML private TextField txtSenha;

    @FXML
    public void initialize() {
        buscarConfiguracoes();
    }

    private void buscarConfiguracoes() {
        String sql = "SELECT email_remetente, senha_app FROM configuracoes WHERE id = 1";
        try (Connection conn = Conexao.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                txtEmail.setText(rs.getString("email_remetente"));
                txtSenha.setText(rs.getString("senha_app"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML
    private void handleSalvar() {
        String sql = "INSERT OR REPLACE INTO configuracoes (id, email_remetente, senha_app) VALUES (1, ?, ?)";
        try (Connection conn = Conexao.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, txtEmail.getText());
            pstmt.setString(2, txtSenha.getText().replaceAll("\\s+", ""));
            pstmt.executeUpdate();

            txtEmail.clear();
            txtSenha.clear();

            ((Stage) txtEmail.getScene().getWindow()).close();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}