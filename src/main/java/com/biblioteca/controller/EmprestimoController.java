package com.biblioteca.controller;

import com.biblioteca.dao.EmprestimoDAO;
import com.biblioteca.dao.LivroDAO;
import com.biblioteca.dao.UsuarioDAO;
import com.biblioteca.model.Emprestimo;
import com.biblioteca.model.Livro;
import com.biblioteca.model.Usuario;
import com.biblioteca.utils.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class EmprestimoController {

    @FXML private TextField txtCpfUsuario;
    @FXML private TextField txtCodigoLivro;
    @FXML private Label lblNomeUsuario;
    @FXML private Label lblTituloLivro;
    @FXML private ComboBox<Integer> comboPrazo;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final LivroDAO livroDAO = new LivroDAO();
    private final EmprestimoDAO emprestimoDAO = new EmprestimoDAO();

    private Usuario usuarioEncontrado;
    private Livro livroEncontrado;

    private final EmailService emailService = new EmailService();

    private final ContextMenu autocompleteCpf = new ContextMenu();
    private final ContextMenu autocompleteCodigo = new ContextMenu();

    private final List<String> listaCpfs = new ArrayList<>();
    private final List<String> listaCodigos = new ArrayList<>();

    @FXML
    public void initialize() {
        comboPrazo.getItems().addAll(7, 15, 30);
        comboPrazo.setValue(7);

        txtCpfUsuario.textProperty().addListener((obs, antigo, novo) -> {
            if (novo.length() == 11) {
                buscarUsuario(novo);
            } else {
                lblNomeUsuario.setText("");
                usuarioEncontrado = null;
            }
        });

        txtCodigoLivro.textProperty().addListener((obs, antigo, novo) -> {
            if (!novo.isEmpty()) {
                buscarLivro(novo);
            } else {
                lblTituloLivro.setText("");
                livroEncontrado = null;
            }
        });

        carregarDadosAutocomplete();

        ComponenteUtils.configurarAutocomplete(txtCpfUsuario, autocompleteCpf, listaCpfs, null);
        ComponenteUtils.configurarAutocomplete(txtCodigoLivro, autocompleteCodigo, listaCodigos, null);
    }

    private void carregarDadosAutocomplete() {
        listaCpfs.clear();
        listaCodigos.clear();

        usuarioDAO.listarTodos().forEach(u -> {
            if (u.getCpf() != null) listaCpfs.add(u.getCpf());
        });

        livroDAO.buscarTodos().forEach(l -> {
            if (l.getCodigo() != null) listaCodigos.add(l.getCodigo());
        });
    }

    private void buscarUsuario(String cpf) {
        usuarioEncontrado = usuarioDAO.buscarPorCpf(cpf);
        if (usuarioEncontrado != null) {
            if (usuarioEncontrado.isBloqueado()) {
                lblNomeUsuario.setText("USUÁRIO BLOQUEADO");
                lblNomeUsuario.setStyle("-fx-text-fill: red;");
            } else {
                lblNomeUsuario.setText(usuarioEncontrado.getNome());
                lblNomeUsuario.setStyle("-fx-text-fill: green;");
            }
        } else {
            lblNomeUsuario.setText("Usuário não encontrado.");
            lblNomeUsuario.setStyle("-fx-text-fill: gray;");
        }
    }

    private void buscarLivro(String codigo) {

        List<Livro> resultados = livroDAO.buscarComFiltros(null, null, codigo, "Disponível");

        if (!resultados.isEmpty()) {
            livroEncontrado = resultados.get(0);
            lblTituloLivro.setText(livroEncontrado.getTitulo());
            lblTituloLivro.setStyle("-fx-text-fill: green;");
        } else {
            lblTituloLivro.setText("Livro indisponível ou não existe.");
            lblTituloLivro.setStyle("-fx-text-fill: red;");
            livroEncontrado = null;
        }
    }

    @FXML
    private void handleFinalizarEmprestimo() {
        if (usuarioEncontrado == null || livroEncontrado == null) {
            mostrarAlerta("Erro", "Selecione um usuário e um livro válidos.");
            return;
        }

        if (usuarioEncontrado.isBloqueado()) {
            mostrarAlerta("Bloqueio", "Este usuário possui pendências e não pode realizar empréstimos.");
            return;
        }

        if (usuarioDAO.possuiEmprestimoAtivo(usuarioEncontrado.getId())) {
            mostrarAlerta("Limite Excedido", "Este usuário já possui um empréstimo pendente.");
            return;
        }

        Emprestimo novoEmprestimo = new Emprestimo(
                livroEncontrado.getId(),
                usuarioEncontrado.getId(),
                comboPrazo.getValue()
        );

        boolean sucesso = emprestimoDAO.realizarEmprestimo(novoEmprestimo);

        if (sucesso) {
            mostrarAlerta("Sucesso", "Empréstimo realizado com sucesso!");
            emailService.enviarConfirmacaoEmprestimo(
                    usuarioEncontrado.getEmail(),
                    usuarioEncontrado.getNome(),
                    livroEncontrado.getTitulo(),
                    novoEmprestimo.getDataDevolucaoPrevista().toString()
            );
            
            limparCampos();
        } else {
            mostrarAlerta("Erro", "Falha técnica ao registrar no banco de dados.");
        }
    }

    private void limparCampos() {
        txtCpfUsuario.clear();
        txtCodigoLivro.clear();
        lblNomeUsuario.setText("");
        lblTituloLivro.setText("");
        usuarioEncontrado = null;
        livroEncontrado = null;
    }

    private void mostrarAlerta(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    @FXML
    private void handleAbrirTelaDevolucao() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/tela-devolucao.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Devolução de Livro");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Erro", "Não foi possível abrir a tela de devolução.");
        }
    }

    @FXML
    private void handleAbrirTelaExtensao() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/modal-extensao.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Estender Prazo de Empréstimo");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Erro", "Não foi possível abrir a tela de extensão de prazo.");
        }
    }
}