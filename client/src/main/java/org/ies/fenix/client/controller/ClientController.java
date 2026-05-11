package org.ies.fenix.client.controller;

import org.ies.fenix.controller.dto.client.ClientLoginDTO;
import org.ies.fenix.controller.dto.client.ClientRegisterDTO;
import org.ies.fenix.controller.dto.client.LoginResponseDTO;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
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

    @FXML
    private Text title;

    @FXML
    private Text subtitle;

    @FXML
    private VBox center;

    @FXML
    private HBox upper;

    @FXML
    private BorderPane root;

    @FXML
    private ImageView logoImage;

    @FXML
    private ImageView settingsImage;

    @FXML
    private Button loginButton;

    @FXML
    private TextField username;

    @FXML
    private PasswordField password;

    @FXML
    private PasswordField passwordCheck;

    private String email = "";

    @FXML
    private Label clientErrorLabel;

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
        clientErrorLabel.setVisible(false);

        username.textProperty().addListener((observable, oldText, newText) -> {
            errorProperty.setValue("");
            clientErrorLabel.setVisible(false);
        });
    }

    @FXML
    public void loadUserAndOpenMarketPlace() {
        clientErrorLabel.setVisible(true);

        String name = username.getText();
        String rawPassword = password.getText();

        if (name == null || name.isBlank()) {
            errorProperty.setValue("Username must not be blank.");
            return;
        }

        if (rawPassword == null || rawPassword.isBlank()) {
            errorProperty.setValue("Password must not be blank.");
            return;
        }

        try {
            ResponseEntity<LoginResponseDTO> response = clientApiService.login(new ClientLoginDTO(name, rawPassword));

            if (response.getStatusCode().value() != 200 || response.getBody().getToken() == null) {
                errorProperty.setValue(response.getBody().getMessage());
                return;
            }

            sessionManager.saveSession(
                    response.getBody().getToken(),
                    response.getBody().getClientId(),
                    response.getBody().getUsername()
            );

            clientErrorLabel.setVisible(false);
            stageManager.switchToNextScene(FxmlView.MARKETPLACE);
        } catch (RuntimeException e) {
            e.printStackTrace();
            errorProperty.setValue("Could not connect to server.");
        }
    }

    @FXML
    public void saveUserAndOpenLogInView() {
        clientErrorLabel.setVisible(true);

        String name = username.getText();
        String rawPassword = password.getText();
        String repeatedPassword = passwordCheck.getText();

        if (name == null || name.isBlank() || name.length() > 20) {
            errorProperty.setValue("Username must not be blank or longer than 20 characters.");
            return;
        }

        if (!rawPassword.equals(repeatedPassword)) {
            errorProperty.setValue("Passwords don't match.");
            return;
        }

        if (rawPassword.length() >= 10) {
            errorProperty.setValue("The password must be less than 10 characters long.");
            return;
        }

        try {
            var response = clientApiService.register(
                    new ClientRegisterDTO(
                            name,
                            email,
                            rawPassword
                    )

            );

            if (response.getStatusCode().value() != 200) {
                errorProperty.setValue(response.getBody().getMessage());
                return;
            }

            clientErrorLabel.setVisible(false);
            stageManager.switchToNextScene(FxmlView.LOGIN);
        } catch (RuntimeException e) {
            e.printStackTrace();
            errorProperty.setValue("Could not connect to server.");
        }
    }

    @FXML
    void switchEmailFormView() {
        stageManager.switchToNextScene(FxmlView.EMAIL);
    }

    void setEmail(String email) {
        this.email= email;
    }
}