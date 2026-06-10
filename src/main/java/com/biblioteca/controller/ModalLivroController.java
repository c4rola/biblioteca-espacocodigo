package com.biblioteca.controller;

import com.biblioteca.dao.LivroDAO;
import com.biblioteca.model.Livro;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ModalLivroController {

    @FXML private TextField txtTitulo;
    @FXML private TextField txtAutor;
    @FXML private TextField txtCodigo;

    private LivroDAO livroDAO = new LivroDAO();

    @FXML
    private void salvarLivro() {
        String titulo = txtTitulo.getText();
        String autor = txtAutor.getText();
        String codigo = txtCodigo.getText();

        if (titulo == null || titulo.trim().isEmpty() ||
                autor == null || autor.trim().isEmpty() ||
                codigo == null || codigo.trim().isEmpty()) {

            exibirAlerta("Erro de Validação", "Todos os campos (Título, Autor e Código) devem ser preenchidos.");
            return;
        }

        Livro novoLivro = new Livro(0, codigo.trim(), titulo.trim(), autor.trim(), true);

        boolean sucesso = livroDAO.salvar(novoLivro);

        if (sucesso) {
            exibirAlerta("Sucesso", "Livro cadastrado com sucesso!");
            Stage stage = (Stage) txtTitulo.getScene().getWindow();
            stage.close();
        } else {
            exibirAlerta("Conflito de Cadastro", "Não foi possível cadastrar: Já existe um livro com este Código.");
        }
    }

    private void exibirAlerta(String titulo, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
