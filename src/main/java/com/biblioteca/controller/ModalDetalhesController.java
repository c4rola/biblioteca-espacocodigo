package com.biblioteca.controller;

import com.biblioteca.model.Usuario;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class ModalDetalhesController {

    @FXML private Label lblId;
    @FXML private TextField txtNome;
    @FXML private TextField txtCpf;
    @FXML private TextField txtEmail;
    @FXML private TextField txtTelefone;
    @FXML private CheckBox chkBloqueado;

    @FXML private Button btnEditar;
    @FXML private Button btnSalvar;
    @FXML private Button btnCancelar;

    private Usuario usuario;
    private final com.biblioteca.dao.UsuarioDAO usuarioDAO = new com.biblioteca.dao.UsuarioDAO();

    @FXML
    private void initialize() {
        configurarMaskTelefone(txtTelefone);
        setEditable(false);
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

    private void setEditable(boolean editable) {
        txtNome.setEditable(editable);
        txtCpf.setEditable(editable);
        txtEmail.setEditable(editable);
        txtTelefone.setEditable(editable);
        chkBloqueado.setDisable(!editable);
        btnSalvar.setDisable(!editable);
        btnCancelar.setDisable(!editable);
        btnEditar.setDisable(editable);
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
        lblId.setText(String.valueOf(usuario.getId()));
        txtNome.setText(usuario.getNome());
        txtCpf.setText(usuario.getCpf());
        txtEmail.setText(usuario.getEmail());
        txtTelefone.setText(usuario.getTelefone());
        chkBloqueado.setSelected(usuario.isBloqueado());
        setEditable(false);
    }

    @FXML
    private void handleEditar() {
        setEditable(true);
    }

    @FXML
    private void handleSalvar() {

        if (txtNome.getText() == null || txtNome.getText().trim().isEmpty()) {
            Alert a = new Alert(Alert.AlertType.WARNING, "O campo Nome não pode estar vazio.", ButtonType.OK);
            a.showAndWait();
            return;
        }

        if ((txtEmail.getText() == null || txtEmail.getText().trim().isEmpty()) &&
                (txtTelefone.getText() == null || txtTelefone.getText().trim().isEmpty())) {
            Alert a = new Alert(Alert.AlertType.WARNING, "É necessário ao menos um meio de contato (E-mail ou Telefone).", ButtonType.OK);
            a.showAndWait();
            return;
        }

        String telefoneInformado = txtTelefone.getText();
        if (telefoneInformado != null && !telefoneInformado.trim().isEmpty()) {
            String onlyDigits = telefoneInformado.replaceAll("\\D", "");
            if (!(onlyDigits.length() == 10 || onlyDigits.length() == 11)) {
                Alert a = new Alert(Alert.AlertType.WARNING, "Telefone inválido. Digite DDD + número (10 ou 11 dígitos).", ButtonType.OK);
                a.showAndWait();
                return;
            }

            telefoneInformado = onlyDigits;
        }

        usuario.setNome(txtNome.getText());
        usuario.setCpf(txtCpf.getText());
        usuario.setEmail(txtEmail.getText());
        usuario.setTelefone(txtTelefone.getText());
        usuario.setBloqueado(chkBloqueado.isSelected());

        boolean sucesso = usuarioDAO.atualizar(usuario);
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
        setUsuario(this.usuario);
    }

    @FXML
    private void fecharModal() {
        lblId.getScene().getWindow().hide();
    }
}
