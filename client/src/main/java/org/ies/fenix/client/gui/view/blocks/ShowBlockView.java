package org.ies.fenix.client.gui.view.blocks;

import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

public class ShowBlockView extends BaseBlockView {

    public ShowBlockView() {

        Label title = new Label("SHOW CHARACTER");

        ComboBox<String> characters = new ComboBox<>();

        characters.getItems().addAll(
                "Sylvie",
                "Eileen",
                "Lucy"
        );

        ComboBox<String> expressions = new ComboBox<>();

        expressions.getItems().addAll(
                "Happy",
                "Sad",
                "Neutral"
        );

        getChildren().addAll(
                title,
                characters,
                expressions
        );
    }
}