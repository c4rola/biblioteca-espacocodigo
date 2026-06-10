package com.biblioteca.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;

public class MenuInicialController {

    @FXML
    private Button btnEntrar;

    @FXML
    private Button btnConfig;

    @FXML
    private void handleEntrar() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/tela-principal.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnEntrar.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Sistema de Gerenciamento - Biblioteca Marcus Félix");
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            System.err.println("Erro ao carregar a tela principal: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleConfig() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/configuracoes.fxml"));
            Parent root = loader.load();
            Stage modalStage = new Stage();
            modalStage.setTitle("Configurações de E-mail");
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initOwner(btnConfig.getScene().getWindow());

            modalStage.setScene(new Scene(root));
            modalStage.setResizable(false);
            modalStage.showAndWait();

        } catch (IOException e) {
            System.err.println("Erro ao abrir configurações: " + e.getMessage());
        }
    }
}