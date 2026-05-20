package org.ies.fenix.client.gui.view.blocks;

import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;

import javafx.scene.control.TextArea;
import org.ies.fenix.client.gui.model.script.DialogBlockModel;
import org.ies.fenix.client.gui.util.FenixCharacter;

import java.util.List;

public class DialogBlockView extends BaseBlockView {

    private ComboBox<FenixCharacter> characterCombo;
    private TextArea textArea;
    private DialogBlockModel model;

    // MODO CATÁLOGO (bloque inactivo)
    public DialogBlockView() {
        Label instruction1 = new Label("SHOW");

        ComboBox<FenixCharacter> previewCombo = new ComboBox<>();
        previewCombo.setDisable(true);

        TextArea previewText = new TextArea();
        previewText.setPromptText("What does the character say?");
        previewText.setDisable(true);

        getChildren().addAll(instruction1, previewCombo, previewText);
    }

    // MODO EDITOR (bloque activo con modelo)
    public DialogBlockView(List<FenixCharacter> characterList, DialogBlockModel model) {
        this.model = model;

        Label instruction1 = new Label("SHOW");

        characterCombo = new ComboBox<>();
        characterCombo.getItems().addAll(characterList);

        characterCombo.valueProperty().addListener((obs, oldValue, newValue) -> {
            model.setCharacter(newValue);
        });

        textArea = new TextArea();
        textArea.setPromptText("What does the character say?");

        textArea.textProperty().addListener((obs, oldValue, newValue) -> {
            model.setDialog(newValue);
        });

        getChildren().addAll(instruction1, characterCombo, textArea);
    }

    public void updateCharacters(List<FenixCharacter> characterList) {
        if (characterCombo != null) {
            characterCombo.getItems().setAll(characterList);
        }
    }
}

