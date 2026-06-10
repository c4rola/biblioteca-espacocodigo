package com.biblioteca.controller;

import com.biblioteca.dao.UsuarioDAO;
import com.biblioteca.model.Usuario;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.Arrays;
import java.util.List;

public class ModalCadastroUsuario {

    @FXML private TextField txtNome;
    @FXML private TextField txtCpf;
    @FXML private TextField txtEmail;
    @FXML private TextField txtTelefone;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    private final ContextMenu autocompleteMenu = new ContextMenu();
    private final List<String> dominios = Arrays.asList("@gmail.com", "@hotmail.com", "@outlook.com", "@yahoo.com", "@icloud.com");

    @FXML
    public void initialize() {
        txtEmail.textProperty().addListener((obs, textoAntigo, textoNovo) -> {
            if (textoNovo != null && textoNovo.endsWith("@")) {
                configurarMenuAutocomplete(textoNovo);
            } else if (!autocompleteMenu.getItems().isEmpty() && (textoNovo == null || !textoNovo.contains("@"))) {
                autocompleteMenu.hide();
            }
        });
        configurarMaskTelefone(txtTelefone);
    }

    private void configurarMaskTelefone(TextField tf) {
        tf.textProperty().addListener((obs, oldText, newText) -> {
            if (newText == null) return;
            String digits = newText.replaceAll("\\D", "");
            if (digits.length() > 11) {
                digits = digits.substring(0, 11);
            }

            String formatted;
            int len = digits.length();

            if (len == 0) {
                formatted = "";
            } else if (len <= 2) {
                formatted = digits;
            } else if (len <= 6) {
                formatted = digits.substring(0, 2) + " " + digits.substring(2);
            } else if (len <= 10) {
                String part1 = digits.substring(0, 2);
                String part2 = digits.substring(2, Math.min(6, len));
                String part3 = digits.substring(Math.min(6, len));
                if (!part3.isEmpty()) {
                    formatted = part1 + " " + part2 + " " + part3;
                } else {
                    formatted = part1 + " " + part2;
                }
            } else {
                formatted = digits.substring(0, 2) + " " + digits.substring(2, 7) + " " + digits.substring(7);
            }

            if (!newText.equals(formatted)) {
                tf.setText(formatted);
                Platform.runLater(() -> tf.positionCaret(formatted.length()));
            }
        });
    }

    private void configurarMenuAutocomplete(String textoAtual) {
        autocompleteMenu.getItems().clear();
        String usuarioParte = textoAtual.substring(0, textoAtual.indexOf("@"));
        for (String dominio : dominios) {
            MenuItem item = new MenuItem(usuarioParte + dominio);
            item.setOnAction(e -> {
                txtEmail.setText(item.getText());
                txtEmail.positionCaret(txtEmail.getText().length());
            });

            autocompleteMenu.getItems().add(item);
        }
        if (!autocompleteMenu.isShowing()) {
            autocompleteMenu.show(txtEmail, javafx.geometry.Side.BOTTOM, 0, 0);
        }
    }

    @FXML
    private void handleSalvarUsuario() {
        String nome = txtNome.getText();
        String cpf = txtCpf.getText();
        String email = txtEmail.getText();
        String telefone = txtTelefone.getText();

        if (nome.isEmpty() || cpf.length() != 11) {
            exibirAlerta("Erro de Validação", "Preencha o nome e um CPF válido (11 dígitos).");
            return;
        }

        if (usuarioDAO.buscarPorCpf(cpf) != null) {
            exibirAlerta("Conflito de Cadastro", "Operação recusada: Já existe um usuário cadastrado com este CPF.");
            return;
        }

        if (email.isEmpty() && (telefone == null || telefone.trim().isEmpty())) {
            exibirAlerta("Erro de validação","É necessário ao menos um meio de contato.");
            return;
        }

        if (telefone != null && !telefone.trim().isEmpty()) {
            String onlyDigits = telefone.replaceAll("\\D", "");
            if (!(onlyDigits.length() == 10 || onlyDigits.length() == 11)) {
                exibirAlerta("Erro de validação", "Telefone inválido. Digite DDD + número.");
                return;
            }
            telefone = onlyDigits;
        }

        Usuario novoUsuario = new Usuario(nome, cpf, email, telefone);

        boolean sucesso = usuarioDAO.salvar(novoUsuario);

        if (sucesso) {
            exibirAlerta("Sucesso", "Usuário cadastrado com sucesso!");
            limparCampos();
            ((Stage) txtEmail.getScene().getWindow()).close();
        } else {
            exibirAlerta("Erro Técnico", "Não foi possível registrar o usuário. Tente novamente.");
        }
    }

    private void limparCampos() {
        txtNome.clear();
        txtCpf.clear();
        txtEmail.clear();
        txtTelefone.clear();
    }

    private void exibirAlerta(String titulo, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}