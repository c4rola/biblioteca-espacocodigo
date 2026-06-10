package com.biblioteca.controller;

import com.biblioteca.dao.EmprestimoDAO;
import com.biblioteca.dao.LivroDAO;
import com.biblioteca.dao.UsuarioDAO;
import com.biblioteca.model.Livro;
import com.biblioteca.utils.ComponenteUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class DevolucaoController {
    @FXML private TextField txtCodigoLivro;
    private final EmprestimoDAO emprestimoDAO = new EmprestimoDAO();
    private final LivroDAO livroDAO = new LivroDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    private final ContextMenu autocompleteCodigo = new ContextMenu();

    private final List<String> listaCodigos = new ArrayList<>();

    @FXML
    public void initialize() {
        carregarDadosAutocomplete();

        ComponenteUtils.configurarAutocomplete(txtCodigoLivro, autocompleteCodigo, listaCodigos, null);
    }

    private void carregarDadosAutocomplete() {
        listaCodigos.clear();

        livroDAO.buscarTodos().forEach(l -> {
            if (l.getCodigo() != null) listaCodigos.add(l.getCodigo());
        });
    }

    @FXML
    private void handleConfirmarDevolucao() {
        String codigo = txtCodigoLivro.getText();

        List<Livro> livros = livroDAO.buscarComFiltros(null, null, codigo, null);

        if (livros.isEmpty()) {
            mostrarAlerta("Erro", "Livro não encontrado.");
            return;
        }

        int idLivro = livros.get(0).getId();
        int idEmprestimo = emprestimoDAO.buscarIdEmprestimoAberto(idLivro);

        if (idEmprestimo != -1) {
            emprestimoDAO.registrarDevolucao(idEmprestimo, idLivro);
            mostrarAlerta("Sucesso", "Livro devolvido com sucesso!");
            txtCodigoLivro.clear();
        } else {
            mostrarAlerta("Aviso", "Este livro já está disponível no sistema.");
        }

        ((Stage) txtCodigoLivro.getScene().getWindow()).close();
    }

    private void mostrarAlerta(String titulo, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setContentText(msg);
        alert.showAndWait();
    }

}