package com.biblioteca.controller;

import com.biblioteca.dao.EmprestimoDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ExtensaoPrazoController {

    @FXML private TextField txtCodigoLivro;
    @FXML private ComboBox<Integer> comboPrazoAdicional;

    private final EmprestimoDAO emprestimoDAO = new EmprestimoDAO();

    @FXML
    public void initialize() {
        comboPrazoAdicional.getItems().addAll(7, 15, 30);
        comboPrazoAdicional.setValue(7); 
    }

    @FXML
    private void handleConfirmarExtensao() {
        String codigo = txtCodigoLivro.getText();
        if (codigo == null || codigo.trim().isEmpty()) {
            mostrarAlerta("Erro", "Informe o código do livro.");
            return;
        }

        int dias = comboPrazoAdicional.getValue();
        boolean sucesso = emprestimoDAO.estenderPrazoEmprestimo(codigo.trim(), dias);

        if (sucesso) {
            mostrarAlerta("Sucesso", "Prazo estendido com sucesso!");
            ((Stage) txtCodigoLivro.getScene().getWindow()).close();
        } else {
            mostrarAlerta("Aviso", "Não foi encontrado nenhum empréstimo ativo para este livro.");
        }
    }

    private void mostrarAlerta(String titulo, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}