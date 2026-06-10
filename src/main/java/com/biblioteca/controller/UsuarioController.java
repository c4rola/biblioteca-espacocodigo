package com.biblioteca.controller;

import com.biblioteca.dao.UsuarioDAO;
import com.biblioteca.model.Usuario;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class UsuarioController {

    @FXML private TableView<Usuario> tabelaUsuarios;
    @FXML private TableColumn<Usuario, Integer> colId;
    @FXML private TableColumn<Usuario, String> colNome;
    @FXML private TableColumn<Usuario, Boolean> colStatus;

    @FXML private TextField txtBuscaId;
    @FXML private TextField txtBuscaNome;
    @FXML private ComboBox<String> comboStatus;

    @FXML private FilteredList<Usuario> filteredData;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private ObservableList<Usuario> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        configurarColunaStatus();
        comboStatus.getItems().addAll("Todos", "Liberado", "Bloqueado");
        comboStatus.setValue("Todos");
        carregarDados();
        this.filteredData = new FilteredList<>(masterData, p -> true);
        tabelaUsuarios.setItems(filteredData);
        tabelaUsuarios.setRowFactory(tv -> {
            TableRow<Usuario> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    abrirModalDetalhes(row.getItem());
                }
            });
            return row;
        });
    }

    private void configurarColunaStatus() {
        colStatus.setCellValueFactory(new PropertyValueFactory<>("bloqueado"));
        colStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean bloqueado, boolean empty) {
                super.updateItem(bloqueado, empty);
                if (empty || bloqueado == null) {
                    setText(null);
                    setStyle("");
                } else {
                    if (bloqueado) {
                        setText("Bloqueado");
                        setStyle("-fx-text-fill: white; -fx-background-color: #ff4444; -fx-alignment: CENTER;");
                    } else {
                        setText("Liberado");
                        setStyle("-fx-text-fill: white; -fx-background-color: #00C851; -fx-alignment: CENTER;");
                    }
                }
            }
        });
    }

    public void carregarDados() {
        masterData.setAll(usuarioDAO.listarTodos());
    }

    private void abrirModalDetalhes(Usuario usuario) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/modal-detalhes.fxml"));
            Parent root = loader.load();
            ModalDetalhesController controller = loader.getController();
            controller.setUsuario(usuario);

            Stage stage = new Stage();
            stage.setTitle("Detalhes do Usuário: " + usuario.getNome());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            carregarDados();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleFiltrar() {
        filteredData.setPredicate(usuario -> {
            String buscaId = txtBuscaId.getText();
            if (buscaId != null && !buscaId.isEmpty()) {
                if (!String.valueOf(usuario.getId()).contains(buscaId)) {
                    return false;
                }
            }
            String buscaNome = txtBuscaNome.getText();
            if (buscaNome != null && !buscaNome.isEmpty()) {
                if (!usuario.getNome().toLowerCase().contains(buscaNome.toLowerCase())) {
                    return false;
                }
            }
            String statusSelecionado = comboStatus.getValue();
            if (statusSelecionado != null && !statusSelecionado.equals("Todos")) {
                boolean filtrarBloqueados = statusSelecionado.equals("Bloqueado");
                if (usuario.isBloqueado() != filtrarBloqueados) {
                    return false;
                }
            }

            return true;
        });
    }

    private void mostrarAlerta(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    @FXML
    private void handleAbrirTelaCadastro() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/modal-cadastro-usuario.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Cadastro de Usuário");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            carregarDados();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Erro", "Não foi possível abrir a tela de cadastro.");
        }
    }

    @FXML
    private void excluirUsuario() {
        Usuario selecionado = tabelaUsuarios.getSelectionModel().getSelectedItem();

        if (selecionado != null) {
            usuarioDAO.excluir(selecionado.getId());

            carregarDados();
        } else {
            System.out.println("Selecione um usuário para excluir!");
        }
    }

}
