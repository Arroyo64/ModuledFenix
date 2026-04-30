package org.ies.fenix.client.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FxmlViewTest {

    @Test
    void getFxmlPath_returnsExpectedPaths() {
        assertEquals("/fxml/login.fxml", FxmlView.LOGIN.getFxmlPath());
        assertEquals("/fxml/email-form.fxml", FxmlView.EMAIL.getFxmlPath());
        assertEquals("/fxml/user-create.fxml", FxmlView.USER_CREATE.getFxmlPath());
        assertEquals("/fxml/marketplace.fxml", FxmlView.MARKETPLACE.getFxmlPath());
        assertEquals("/fxml/library.fxml", FxmlView.LIBRARY.getFxmlPath());
        assertEquals("/fxml/game.fxml", FxmlView.GAME.getFxmlPath());
        assertEquals("/fxml/profile.fxml", FxmlView.PROFILE.getFxmlPath());
    }

    @Test
    void fxmlLoader_throwsExceptionWhenResourceDoesNotExist() {
        FxmlLoader fxmlLoader = new FxmlLoader();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> fxmlLoader.load("/fxml/not-found.fxml")
        );

        assertEquals("FXML not found: /fxml/not-found.fxml", exception.getMessage());
    }
}
