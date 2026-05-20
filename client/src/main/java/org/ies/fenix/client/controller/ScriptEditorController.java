package org.ies.fenix.client.controller;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import org.ies.fenix.client.gui.view.blocks.*;
import org.ies.fenix.client.gui.util.BlockFactory;

public class ScriptEditorController {

    @FXML
    private VBox workspace;

    @FXML
    public void addNarrativeBlock() {
        workspace.getChildren().add(
                BlockFactory.createNarrativeBlock()
        );
    }

    @FXML
    public void addShowBlock() {
        workspace.getChildren().add(
                BlockFactory.createShowBlock()
        );
    }

    @FXML
    public void addDecisionBlock() {
        workspace.getChildren().add(
                BlockFactory.createDecisionBlock()
        );
    }


    @FXML
    public void addBackgroundBlock() {
        workspace.getChildren().add(
                BlockFactory.createBackgroundBlock()
        );
    }

//    @FXML
//    public void addMusicBlock() {
//        workspace.getChildren().add(
//                BlockFactory.createMusicBlock()
//        );
//    } for later todo
}
