package com.biblioteca.controller;

import com.biblioteca.dao.EmprestimoDAO;
import com.biblioteca.model.RelatorioDTO;
import com.biblioteca.utils.DateUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;

public class RelatorioController {

    @FXML private TableView<RelatorioDTO> tabelaRelatorio;
    @FXML private TableColumn<RelatorioDTO, String> colCodigo;
    @FXML private TableColumn<RelatorioDTO, String> colUsuario;
    @FXML private TableColumn<RelatorioDTO, String> colTitulo;
    @FXML private TableColumn<RelatorioDTO, LocalDate> colDataSaida;
    @FXML private TableColumn<RelatorioDTO, LocalDate> colDataPrevista;
    @FXML private TableColumn<RelatorioDTO, String> colStatus;


    @FXML private TextField txtBusca;
    @FXML private ComboBox<String> comboOpcaoBusca;
    @FXML private CheckBox checkAtrasados;

    private final EmprestimoDAO emprestimoDAO = new EmprestimoDAO();
    private ObservableList<RelatorioDTO> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigoLivro"));
        colUsuario.setCellValueFactory(new PropertyValueFactory<>("nomeUsuario"));
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("tituloLivro"));
        colDataSaida.setCellValueFactory(new PropertyValueFactory<>("dataEmprestimo"));
        colDataPrevista.setCellValueFactory(new PropertyValueFactory<>("dataPrevista"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        colDataSaida.setCellFactory(column -> new TableCell<RelatorioDTO, LocalDate>() {

            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(DateUtils.format(item));
                }
            }
        });
        colDataPrevista.setCellFactory(column -> new TableCell<RelatorioDTO, LocalDate>() {

            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);

                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(DateUtils.format(item));
                }
            }
        });
        comboOpcaoBusca.getItems().addAll("Título", "Código", "Usuário");
        comboOpcaoBusca.setValue("Título");
        carregarDados();
        FilteredList<RelatorioDTO> filteredData = new FilteredList<>(masterData, p -> true);
        txtBusca.textProperty().addListener((obs, oldVal, newVal) -> {
            configurarFiltro(filteredData);
        });
        checkAtrasados.selectedProperty().addListener((obs, oldVal, newVal) -> {
            configurarFiltro(filteredData);
        });
        tabelaRelatorio.setItems(filteredData);
        configurarCoresStatus();
    }

    public void carregarDados() {
        masterData.setAll(emprestimoDAO.listarRelatorio());
    }

    private void configurarFiltro(FilteredList<RelatorioDTO> filteredData) {
        filteredData.setPredicate(relatorio -> {
            String busca = txtBusca.getText();
            boolean matchBusca = false;

            if (busca == null || busca.isEmpty()) {
                matchBusca = true;
            } else {
                String filtro = comboOpcaoBusca.getValue();
                String lowCase = busca.toLowerCase();

                if (filtro.equals("Título")) matchBusca = relatorio.getTituloLivro().toLowerCase().contains(lowCase);
                else if (filtro.equals("Código")) matchBusca = relatorio.getCodigoLivro().contains(lowCase);
                else if (filtro.equals("Usuário")) matchBusca = relatorio.getNomeUsuario().toLowerCase().contains(lowCase);
            }

            if (checkAtrasados.isSelected()) {
                return matchBusca && relatorio.getStatus().contains("Atrasado");
            }
            return matchBusca;
        });
    }

    private void configurarCoresStatus() {
        colStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.contains("Atrasado")) {
                        setStyle("-fx-background-color: #ffcccc; -fx-text-fill: red; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: green;");
                    }
                }
            }
        });
    }
}