package org.ies.fenix.client.controller;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.ProgressBar;
import org.ies.fenix.client.config.StageManager;
import org.ies.fenix.client.controller.BaseLayoutController;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class GlobalProgressTest {

    @BeforeAll
    public static void initJavaFX() {
        // Necesario para inicializar JavaFX en tests
        new JFXPanel();
    }

    @Test
    public void testGlobalProgressBar() throws Exception {

        // Crear BaseLayoutController manualmente
        BaseLayoutController base = new BaseLayoutController();

        // Simular inyección del ProgressBar
        ProgressBar bar = new ProgressBar();
        bar.setVisible(false);

        // Inyectar manualmente (como si fuera @FXML)
        var field = BaseLayoutController.class.getDeclaredField("globalProgress");
        field.setAccessible(true);
        field.set(base, bar);

        // Simular StageManager mínimo
        StageManager fakeManager = new StageManager(null, null, "Test", null);
        var field2 = StageManager.class.getDeclaredField("baseLayoutController");
        field2.setAccessible(true);
        field2.set(fakeManager, base);

        System.out.println("=== TEST: Barra global ===");

        // Mostrar barra
        Platform.runLater(() -> {
            base.showProgress();
            System.out.println("Visible (esperado true): " + bar.isVisible());
            System.out.println("Progreso (esperado -1): " + bar.getProgress());
        });

        Thread.sleep(300);

        // Simular progreso real
        for (int i = 0; i <= 10; i++) {
            double p = i / 10.0;
            double finalP = p;

            Platform.runLater(() -> {
                base.setProgress(finalP);
                System.out.println("Progreso: " + bar.getProgress());
            });

            Thread.sleep(150);
        }

        // Ocultar barra
        Platform.runLater(() -> {
            base.hideProgress();
            System.out.println("Visible (esperado false): " + bar.isVisible());
        });

        Thread.sleep(300);
    }
}