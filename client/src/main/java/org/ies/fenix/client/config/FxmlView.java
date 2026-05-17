package org.ies.fenix.client.config;

public enum FxmlView {

    LOGIN(false) {
        @Override
        public String getFxmlPath() {
            return "/fxml/login.fxml";
        }
    },

    EMAIL(false) {
        @Override
        public String getFxmlPath() {
            return "/fxml/email-form.fxml";
        }
    },

    USER_CREATE(false) {
        @Override
        public String getFxmlPath() {
            return "/fxml/user-create.fxml";
        }
    },

    MARKETPLACE(true) {
        @Override
        public String getFxmlPath() {
            return "/fxml/marketplace.fxml";
        }
    },

    LIBRARY(true) {
        @Override
        public String getFxmlPath() {
            return "/fxml/library.fxml";
        }
    },

    GAME(true) {
        @Override
        public String getFxmlPath() {
            return "/fxml/game.fxml";
        }
    },

    PROFILE(true) {
        @Override
        public String getFxmlPath() {
            return "/fxml/profile.fxml";
        }
    },

    UPLOAD_GAME(false) {
        @Override
        public String getFxmlPath() {
            return "/fxml/upload-game.fxml";
        }
    };

    private final boolean useBaseLayout;

    FxmlView(boolean useBaseLayout) {
        this.useBaseLayout = useBaseLayout;
    }

    public boolean usesBaseLayout() {
        return useBaseLayout;
    }

    public abstract String getFxmlPath();
}
