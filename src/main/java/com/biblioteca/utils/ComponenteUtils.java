package com.biblioteca.utils;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;

import java.util.List;

public class ComponenteUtils {

    public static void configurarAutocomplete(TextField campo, ContextMenu menu, List<String> listaDados, Runnable acaoAoSelecionar) {

        campo.textProperty().addListener((obs, antigo, novo) -> {
            if (novo == null || novo.trim().length() < 2) {
                menu.hide();
                return;
            }

            menu.getItems().clear();
            String termoBusca = novo.trim().toLowerCase();
            listaDados.stream()
                    .filter(item -> item != null && item.toLowerCase().contains(termoBusca))
                    .distinct()
                    .limit(5)
                    .forEach(sugestao -> {
                        MenuItem itemMenu = new MenuItem(sugestao);
                        itemMenu.setOnAction(e -> {
                            campo.setText(sugestao);
                            campo.positionCaret(sugestao.length());
                            if (acaoAoSelecionar != null) {
                                acaoAoSelecionar.run();
                            }
                        });
                        menu.getItems().add(itemMenu);
                    });

            if (!menu.getItems().isEmpty()) {
                if (!menu.isShowing()) {
                    menu.show(campo, javafx.geometry.Side.BOTTOM, 0, 0);
                }
            } else {
                menu.hide();
            }
        });
        campo.focusedProperty().addListener((obs, antigo, focado) -> {
            if (!focado) {
                menu.hide();
            }
        });
    }
}
