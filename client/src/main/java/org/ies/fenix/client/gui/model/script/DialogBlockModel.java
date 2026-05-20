package org.ies.fenix.client.gui.model.script;

import org.ies.fenix.client.gui.util.FenixCharacter;

public class DialogBlockModel {
    private String dialog;

    public String getDialog() {
        return dialog;
    }

    public void setDialog(String dialog) {
        this.dialog = dialog;
    }

    public FenixCharacter getCharacter() {
        return character;
    }

    public void setCharacter(FenixCharacter character) {
        this.character = character;
    }

    private FenixCharacter character;

}
