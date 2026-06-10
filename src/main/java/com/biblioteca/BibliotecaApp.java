package com.biblioteca;

import com.biblioteca.dao.Conexao;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class BibliotecaApp extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(BibliotecaApp.class.getResource("/fxml/menu-inicial.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 882, 688);

        stage.setTitle("Biblioteca Marcus Félix - Início");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        Conexao.criarTabelasIniciais();
        launch();
    }
}