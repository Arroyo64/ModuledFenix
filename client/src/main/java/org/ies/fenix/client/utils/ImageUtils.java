package org.ies.fenix.client.utils;

import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Hyperlink;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import org.ies.fenix.client.api.SessionManager;
import org.ies.fenix.controller.IClientController;
import org.kordamp.ikonli.javafx.FontIcon;
import org.springframework.http.ResponseEntity;

import java.io.ByteArrayInputStream;

public class ImageUtils {

    private ImageUtils() {
    }

    private static void applyCoverCrop(ImageView imageView, Image image, double size) {

        imageView.setFitWidth(size);
        imageView.setFitHeight(size);
        imageView.setSmooth(true);
        imageView.setPreserveRatio(false);

        double imgW = image.getWidth();
        double imgH = image.getHeight();

        double ratio = Math.max(size / imgW, size / imgH);

        double newW = size / ratio;
        double newH = size / ratio;

        double x = (imgW - newW) / 2;
        double y = (imgH - newH) / 2;

        imageView.setViewport(new Rectangle2D(x, y, newW, newH));

        imageView.setImage(image);
    }

    public static void setCoverImage(
            byte[] imageBytes,
            ImageView imageView,
            double width,
            double height
    ) {
        if (imageBytes != null && imageBytes.length > 0) {
            Image image = new Image(new ByteArrayInputStream(imageBytes));

            imageView.setFitWidth(width);
            imageView.setFitHeight(height);
            imageView.setSmooth(true);
            imageView.setPreserveRatio(false);

            double imgW = image.getWidth();
            double imgH = image.getHeight();

            double ratio = Math.max(width / imgW, height / imgH);

            double newW = width / ratio;
            double newH = height / ratio;

            double x = (imgW - newW) / 2;
            double y = (imgH - newH) / 2;

            imageView.setViewport(new Rectangle2D(x, y, newW, newH));
            imageView.setClip(null);
            imageView.setImage(image);
            imageView.setVisible(true);

        } else {
            imageView.setVisible(false);
        }
    }

    public static void setAvatar(
            byte[] imageBytes,
            ImageView imageView,
            FontIcon icon,
            double size
    ) {

        if (imageBytes != null && imageBytes.length > 0) {

            Image image = new Image(new ByteArrayInputStream(imageBytes));

            // Esperar a que el ImageView esté renderizado
            Platform.runLater(() -> {

                applyCoverCrop(imageView, image, size);

                Circle clip = new Circle(size / 2);
                clip.setCenterX(size / 2);
                clip.setCenterY(size / 2);

                imageView.setClip(clip);

                imageView.setVisible(true);
                icon.setVisible(false);
            });

        } else {
            imageView.setVisible(false);
            icon.setVisible(true);
        }
    }

    public static void setAvatar(
            byte[] imageBytes,
            ImageView imageView,
            double size
    ) {
        if (imageBytes != null && imageBytes.length > 0) {

            Image image = new Image(new ByteArrayInputStream(imageBytes));

            Platform.runLater(() -> {

                applyCoverCrop(imageView, image, size);

                Circle clip = new Circle(size / 2);
                clip.setCenterX(size / 2);
                clip.setCenterY(size / 2);

                imageView.setClip(clip);
                imageView.setVisible(true);
            });

        } else {
            imageView.setVisible(false);
        }
    }


    public static void setCoverImage(
            byte[] imageBytes,
            ImageView imageView,
            double size
    ) {

        if (imageBytes != null && imageBytes.length > 0) {

            Image image = new Image(new ByteArrayInputStream(imageBytes));

            imageView.setFitWidth(size);
            imageView.setFitHeight(size);
            imageView.setSmooth(true);
            imageView.setPreserveRatio(false);

            double imgW = image.getWidth();
            double imgH = image.getHeight();

            double ratio = Math.max(size / imgW, size / imgH);

            double newW = size / ratio;
            double newH = size / ratio;

            double x = (imgW - newW) / 2;
            double y = (imgH - newH) / 2;

            imageView.setViewport(new Rectangle2D(x, y, newW, newH));

            imageView.setClip(null); // importante: quitar cualquier clip previo

            imageView.setImage(image);
            imageView.setVisible(true);

        } else {
            imageView.setVisible(false);
        }
    }
    public static void initialConfig(
            IClientController clientApiService,
            SessionManager sessionManager,
            Hyperlink username,
            ImageView topProfileImage,
            FontIcon topProfileIcon
    ) {
        if (username != null && sessionManager.getUsername() != null) {
            username.setText(sessionManager.getUsername().toUpperCase());
        }

        if (topProfileImage == null || topProfileIcon == null) {
            return;
        }

        try {
            ResponseEntity<?> image =
                    clientApiService.getProfileImage(sessionManager.getAuthorizationHeader());

            if (image.getStatusCode().is2xxSuccessful() && image.getBody() instanceof byte[] bytes) {
                setAvatar(bytes, topProfileImage, topProfileIcon, 40);
            }

        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }
    public static Image loadImage(ResponseEntity<byte[]> response) {
        try {
            if (response == null) return null;

            if (response.getStatusCode().is2xxSuccessful()) {
                byte[] bytes = response.getBody();
                if (bytes == null) return null;
                return new Image(new ByteArrayInputStream(bytes));
            } else {
                // Convertir el error (que viene como byte[]) a String
                byte[] errorBytes = response.getBody();
                String error = errorBytes != null ? new String(errorBytes) : "Unknown error";
                System.out.println("API Image Error: " + error);
                return null;
            }

        } catch (Exception e) {
            System.out.println("Exception loading image: " + e.getMessage());
            return null;
        }
    }
}