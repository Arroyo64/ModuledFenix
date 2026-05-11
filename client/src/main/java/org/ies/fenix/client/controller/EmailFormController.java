package org.ies.fenix.client.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.ies.fenix.client.config.FxmlView;
import org.ies.fenix.client.config.StageManager;


import java.net.URL;
import java.util.ResourceBundle;

import static org.ies.fenix.client.utils.EmailValidator.isValidEmail;

public class EmailFormController implements Initializable {

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
    private Button continueButton;

    @FXML
    private Button backButton;

    @FXML
    public TextField emailTextField;

    @FXML
    private Label clientErrorLabel;

    private final StringProperty errorProperty = new SimpleStringProperty();

    private final StageManager stageManager;

    public EmailFormController(StageManager stageManager) {
        this.stageManager = stageManager;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        clientErrorLabel.textProperty().bind(errorProperty);
        clientErrorLabel.visibleProperty().bind(
                errorProperty.isNotNull().and(errorProperty.isNotEmpty())
        );
        clientErrorLabel.managedProperty().bind(clientErrorLabel.visibleProperty());
        emailTextField.textProperty().addListener((observable, oldText, newText) -> {
            errorProperty.setValue("");
        });
        logoImage.setFitWidth(294.0);
        logoImage.setSmooth(true);
        settingsImage.setFitWidth(15.0);
        settingsImage.setSmooth(true);
    }

    @FXML
    void switchToUserCreateView(){
        String email = emailTextField.getText();
        if(!isValidEmail(email)){
            errorProperty.set("Please enter a valid email address");
            return;
        }
        ClientController controller =
                stageManager.switchSceneAndGetController(FxmlView.USER_CREATE);
        controller.setEmail(email);
    }

    @FXML
    void switchLogInView(){ stageManager.switchToNextScene(FxmlView.LOGIN);}



}
