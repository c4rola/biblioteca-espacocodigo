package com.biblioteca.controller;

import com.biblioteca.dao.LivroDAO;
import com.biblioteca.model.Livro;
import com.biblioteca.model.Usuario;
import com.biblioteca.utils.ComponenteUtils;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TelaPrincipalController {

    @FXML private TableView<Livro> tabelaLivros;
    @FXML private TableColumn<Livro, String> colCodigo;
    @FXML private TableColumn<Livro, String> colTitulo;
    @FXML private TableColumn<Livro, String> colAutor;
    @FXML private TableColumn<Livro, String> colStatus;
    @FXML private TextField txtFiltroTitulo, txtFiltroAutor, txtFiltroCodigo;
    @FXML private ComboBox<String> comboFiltroStatus;

    @FXML private Tab tabRelatorio;
    @FXML private Tab tabUsuarios;
    @FXML private Tab tabLivros;
    @FXML private RelatorioController relatorioController;
    @FXML private UsuarioController usuarioController;

    private final ContextMenu autocompleteTitulo = new ContextMenu();
    private final ContextMenu autocompleteAutor = new ContextMenu();
    private final ContextMenu autocompleteCodigo = new ContextMenu();

    private final List<String> listaTitulos = new ArrayList<>();
    private final List<String> listaAutores = new ArrayList<>();
    private final List<String> listaCodigos = new ArrayList<>();

    private LivroDAO livroDAO = new LivroDAO();

    @FXML
    public void initialize() {
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colAutor.setCellValueFactory(new PropertyValueFactory<>("autor"));
        colStatus.setCellValueFactory(cellData -> {
            boolean disponivel = cellData.getValue().isDisponivel();
            return new SimpleStringProperty(disponivel ? "Disponível" : "Indisponível");
        });

        comboFiltroStatus.getItems().addAll("Todos", "Disponível", "Indisponível");
        comboFiltroStatus.setValue("Todos");

        tabRelatorio.selectedProperty().addListener((obs, old, isSelected) -> {
            if (isSelected) {
                relatorioController.carregarDados();
            }
        });

        tabUsuarios.selectedProperty().addListener((obs, old, isSelected) -> {
            if (isSelected && usuarioController != null) {
                usuarioController.carregarDados(); 
            }
        });

        tabLivros.selectedProperty().addListener((obs, old, isSelected) -> {
            if (isSelected) {
                atualizarTabela();
            }
        });

        tabelaLivros.setRowFactory(tv -> {
            TableRow<Livro> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    abrirModalDetalhes(row.getItem());
                }
            });
            return row;
        });

        atualizarTabela();

        ComponenteUtils.configurarAutocomplete(txtFiltroTitulo, autocompleteTitulo, listaTitulos, this::handleBuscar);
        ComponenteUtils.configurarAutocomplete(txtFiltroAutor, autocompleteAutor, listaAutores, this::handleBuscar);
        ComponenteUtils.configurarAutocomplete(txtFiltroCodigo, autocompleteCodigo, listaCodigos, this::handleBuscar);

    }

    @FXML
    private void abrirModalLivro() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/modal-livro.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Cadastrar Novo Livro");
            stage.showAndWait();
            atualizarTabela();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void atualizarTabela() {
        List<Livro> livrosDoBanco = livroDAO.buscarTodos();

        ObservableList<Livro> livros = FXCollections.observableArrayList(livroDAO.buscarTodos());
        tabelaLivros.setItems(livros);
        carregarSugestoesMemoria(livrosDoBanco);
    }

    private void carregarSugestoesMemoria(List<Livro> todosLivros) {
        listaTitulos.clear();
        listaAutores.clear();
        listaCodigos.clear();

        for (Livro livro : todosLivros) {
            if (livro.getTitulo() != null) listaTitulos.add(livro.getTitulo());
            if (livro.getAutor() != null) listaAutores.add(livro.getAutor());
            if (livro.getCodigo() != null) listaCodigos.add(livro.getCodigo());
        }
    }

    @FXML
    private void excluirLivro() {
        Livro selecionado = tabelaLivros.getSelectionModel().getSelectedItem();

        if (selecionado != null) {
            livroDAO.excluir(selecionado.getId());
            atualizarTabela();
        } else {
            System.out.println("Selecione um livro para excluir!");
        }
    }

    @FXML
    private void handleBuscar() {
        String titulo = txtFiltroTitulo.getText();
        String autor = txtFiltroAutor.getText();
        String codigo = txtFiltroCodigo.getText();
        String status = comboFiltroStatus.getValue();
        List<Livro> resultados = livroDAO.buscarComFiltros(titulo, autor, codigo, status);
        ObservableList<Livro> listaExibicao = FXCollections.observableArrayList(resultados);
        tabelaLivros.setItems(listaExibicao);
    }

    @FXML
    private void handleImportarExcel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecionar Planilha de Livros (Excel)");

        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Planilhas do Excel (*.xlsx)", "*.xlsx")
        );

        Stage stage = (Stage) tabelaLivros.getScene().getWindow();
        File arquivoSelecionado = fileChooser.showOpenDialog(stage);

        if (arquivoSelecionado != null) {

            int livrosImportados = com.biblioteca.utils.ExcelUtils.importarLivros(arquivoSelecionado.getAbsolutePath());

            if (livrosImportados > 0) {
                exibirAlerta("Importação Concluída", "Sucesso! " + livrosImportados + " livros foram processados/atualizados no sistema.");
                atualizarTabela();
            } else if (livrosImportados == 0) {
                exibirAlerta("Aviso", "Nenhum livro novo foi importado. Verifique os códigos ou se a tabela possui dados válidos.");
            } else {
                exibirAlerta("Erro", "Falha técnica ao processar a planilha.");
            }
        }
    }

    private void abrirModalDetalhes(Livro livro) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/modal-detalhes-livro.fxml"));
            Parent root = loader.load();
            ModalDetalhesLivroController controller = loader.getController();
            controller.setLivro(livro);

            Stage stage = new Stage();
            stage.setTitle("Detalhes do Livro: " + livro.getTitulo());
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
            atualizarTabela();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void exibirAlerta(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }
}