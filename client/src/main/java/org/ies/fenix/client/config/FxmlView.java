package org.ies.fenix.client.config;

public enum FxmlView {


    LOGIN {

        @Override
        public String getFxmlPath() {
            return "/fxml/login.fxml";
        }
    },

    EMAIL {

        @Override
        public String getFxmlPath() {
            return "/fxml/email-form.fxml";
        }
    },

    USER_CREATE {

        @Override
        public String getFxmlPath() {
            return "/fxml/user-create.fxml";
        }
    },

    MARKETPLACE {

        @Override
        public String getFxmlPath() {
            return "/fxml/marketplace.fxml";
        }
    },

    LIBRARY {
        @Override
        public String getFxmlPath() {
            return "/fxml/library.fxml";
        }
    },

    GAME {
        @Override
        public String getFxmlPath() {
            return "/fxml/game.fxml";
        }
    },
    PROFILE {
        @Override
        public String getFxmlPath() {
            return "/fxml/profile.fxml";
        }
    };



    public abstract String getFxmlPath();
}
