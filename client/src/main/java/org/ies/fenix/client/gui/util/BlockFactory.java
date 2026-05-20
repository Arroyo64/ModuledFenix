package org.ies.fenix.client.gui.util;

import org.ies.fenix.client.gui.view.blocks.*;

public class BlockFactory {

    public static NarrativeBlockView createNarrativeBlock() {
        return new NarrativeBlockView();
    }

    public static ShowBlockView createShowBlock() {
        return new ShowBlockView();
    }

    public static SceneBlockView createSceneBlock() {
        return new SceneBlockView();
    }

    public static OptionBlockView createOptionBlock(){
        return new OptionBlockView();
    }

    public static DecisionBlockView createDecisionBlock() {
        return new DecisionBlockView();
    }

    public static BackgroundBlockView createBackgroundBlock() {
        return new BackgroundBlockView();
    }
}