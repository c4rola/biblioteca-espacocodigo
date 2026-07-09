package com.biblioteca.controller;

import com.biblioteca.model.Livro;
import com.biblioteca.model.Usuario;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class ModalDetalhesLivroController {

    @FXML private TextField txtCodigo;
    @FXML private TextField txtTitulo;
    @FXML private TextField txtAutor;
    @FXML private CheckBox chkDisponivel;

    @FXML private Button btnEditar;
    @FXML private Button btnSalvar;
    @FXML private Button btnCancelar;

    private Livro livro;
    private final com.biblioteca.dao.LivroDAO livroDAO = new com.biblioteca.dao.LivroDAO();

    @FXML
    private void initialize() {
        setEditable(false);
    }

    private void setEditable(boolean editable) {
        txtCodigo.setEditable(editable);
        txtTitulo.setEditable(editable);
        txtAutor.setEditable(editable);
        chkDisponivel.setDisable(!editable);
        btnSalvar.setDisable(!editable);
        btnCancelar.setDisable(!editable);
        btnEditar.setDisable(editable);
    }

    public void setLivro(Livro livro) {
        this.livro = livro;
        txtCodigo.setText(livro.getCodigo());
        txtTitulo.setText(livro.getTitulo());
        txtAutor.setText(livro.getAutor());
        chkDisponivel.setSelected(livro.isDisponivel());
        setEditable(false);
    }

    @FXML
    private void handleEditar() {
        setEditable(true);
    }

    @FXML
    private void handleSalvar() {

        if (txtCodigo.getText() == null || txtCodigo.getText().trim().isEmpty()) {
            Alert a = new Alert(Alert.AlertType.WARNING, "O campo Código não pode estar vazio.", ButtonType.OK);
            a.showAndWait();
            return;
        }

        if (txtTitulo.getText() == null || txtTitulo.getText().trim().isEmpty()){
            Alert a = new Alert(Alert.AlertType.WARNING, "O campo Título não pode estar vazio.", ButtonType.OK);
            a.showAndWait();
            return;
        }

        livro.setCodigo(txtCodigo.getText());
        livro.setTitulo(txtTitulo.getText());
        livro.setAutor(txtAutor.getText());
        livro.setDisponivel(chkDisponivel.isSelected());

        boolean sucesso = livroDAO.atualizar(livro);
        if (sucesso) {
            setEditable(false);
            fecharModal();
        } else {
            Alert a = new Alert(Alert.AlertType.ERROR, "Erro ao salvar alterações.", ButtonType.OK);
            a.showAndWait();
        }
    }

    @FXML
    private void handleCancelar() {
        setLivro(this.livro);
    }

    @FXML
    private void fecharModal() {
        txtCodigo.getScene().getWindow().hide();
    }
}