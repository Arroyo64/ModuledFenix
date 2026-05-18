package org.ies.fenix.client.controller;

import org.ies.fenix.controller.dto.client.ClientLoginDTO;
import org.ies.fenix.controller.dto.client.ClientRegisterDTO;
import org.ies.fenix.controller.dto.client.LoginResponseDTO;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.ies.fenix.client.api.SessionManager;
import org.ies.fenix.client.config.FxmlView;
import org.ies.fenix.client.config.StageManager;
import org.ies.fenix.controller.IClientController;
import org.springframework.http.ResponseEntity;

import java.net.URL;
import java.util.ResourceBundle;

public class ClientController implements Initializable {

    @FXML private Text title;
    @FXML private Text subtitle;
    @FXML private VBox center;
    @FXML private HBox upper;
    @FXML private BorderPane root;
    @FXML private ImageView logoImage;
    @FXML private ImageView settingsImage;
    @FXML private Button loginButton;
    @FXML private TextField username;
    @FXML private PasswordField password;
    @FXML private PasswordField passwordCheck;
    @FXML private Label clientErrorLabel;

    private String email = "";
    private final StringProperty errorProperty = new SimpleStringProperty();

    private final StageManager stageManager;
    private final IClientController clientApiService;
    private final SessionManager sessionManager;

    public ClientController(StageManager stageManager,
                            IClientController clientApiService,
                            SessionManager sessionManager) {
        this.stageManager = stageManager;
        this.clientApiService = clientApiService;
        this.sessionManager = sessionManager;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        clientErrorLabel.textProperty().bind(errorProperty);
        clientErrorLabel.visibleProperty().bind(errorProperty.isNotEmpty());
        clientErrorLabel.managedProperty().bind(clientErrorLabel.visibleProperty());

        username.textProperty().addListener((obs, oldVal, newVal) -> errorProperty.set(""));
        password.textProperty().addListener((obs, oldVal, newVal) -> errorProperty.set(""));
        if (passwordCheck != null) {
            passwordCheck.textProperty().addListener((obs, oldVal, newVal) -> errorProperty.set(""));
        }

    }

    // ============================================================
    //                      LOGIN
    // ============================================================

    public void loadUserAndOpenMarketPlace() {

        String name = username.getText();
        String rawPassword = password.getText();

        if (name.isBlank()) {
            errorProperty.set("Username cannot be blank.");
            return;
        }

        if (rawPassword.isBlank()) {
            errorProperty.set("Password cannot be blank.");
            return;
        }

        try {
            ResponseEntity<LoginResponseDTO> response =
                    clientApiService.login(new ClientLoginDTO(name, rawPassword));

            if (!response.getStatusCode().is2xxSuccessful()) {
                errorProperty.set(response.getBody() != null
                        ? response.getBody().getMessage()
                        : "Login failed.");
                return;
            }

            LoginResponseDTO body = response.getBody();
            if (body == null || body.getToken() == null) {
                errorProperty.set("Invalid server response.");
                return;
            }

            sessionManager.saveSession(
                    body.getToken(),
                    body.getClientId(),
                    body.getUsername()
            );

            errorProperty.set("");
            stageManager.switchScene(FxmlView.MARKETPLACE);

        } catch (Exception e) {
            errorProperty.set("Could not connect to server.");
        }
    }

    // ============================================================
    //                      REGISTER
    // ============================================================

    public void saveUserAndOpenLogInView() {

        String name = username.getText();
        String rawPassword = password.getText();
        String repeatedPassword = passwordCheck.getText();

        if (name.isBlank() || name.length() > 20) {
            errorProperty.set("Username must not be blank or longer than 20 characters.");
            return;
        }

        if (!rawPassword.equals(repeatedPassword)) {
            errorProperty.set("Passwords do not match.");
            return;
        }

        if (rawPassword.length() >= 10) {
            errorProperty.set("Password must be less than 10 characters.");
            return;
        }

        try {
            ResponseEntity<?> response = clientApiService.register(
                    new ClientRegisterDTO(name, email, rawPassword)
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                Object body = response.getBody();
                if (body instanceof String msg) {
                    errorProperty.set(msg);
                } else {
                    errorProperty.set("Registration failed.");
                }
                return;
            }

            errorProperty.set("");
            stageManager.switchScene(FxmlView.LOGIN);

        } catch (Exception e) {
            errorProperty.set("Could not connect to server.");
        }
    }

    // ============================================================
    //                      NAVIGATION
    // ============================================================

    @FXML
    void switchEmailFormView() {
        stageManager.switchScene(FxmlView.EMAIL);
    }

    void setEmail(String email) {
        this.email = email;
    }
}