package org.ies.fenix.client.gui.view.blocks;

import javafx.geometry.Insets;
import javafx.scene.layout.VBox;

public abstract class BaseBlockView extends VBox {

    public BaseBlockView() {

        setSpacing(10);

        setPadding(new Insets(15));

        getStyleClass().add("block");
    }
}
