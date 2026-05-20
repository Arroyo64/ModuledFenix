package org.ies.fenix.client.gui.view.blocks;


import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import org.ies.fenix.client.gui.model.script.NarrativeBlockModel;

public class NarrativeBlockView extends BaseBlockView {

    private TextArea textArea;
    private NarrativeBlockModel model;

    //metodo para mostrar en el espacio de trabajo
    public NarrativeBlockView(NarrativeBlockModel model) {
        this.model = model;

        Label title = new Label("TEXT");

        textArea = new TextArea();
        textArea.setPromptText("Describe the scene...");

        // Listener: actualiza el modelo cuando el usuario escribe
        textArea.textProperty().addListener((obs, oldValue, newValue) -> {
            model.setNarration(newValue);
        });

        getChildren().addAll(title, textArea);
    }

    //metodo para mostrar en el catalogo
    public NarrativeBlockView() {
        Label title = new Label("TEXT");
        TextArea textArea = new TextArea();
        textArea.setDisable(true); // opcional: para que no se pueda escribir
        getChildren().addAll(title, textArea);
    }


    // Si quieres cargar datos existentes (por ejemplo al editar un script)
    public void loadFromModel() {
        textArea.setText(model.getNarration());
    }
}

