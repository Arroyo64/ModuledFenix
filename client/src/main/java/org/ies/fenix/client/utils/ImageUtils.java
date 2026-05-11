package org.ies.fenix.client.utils;

import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import org.kordamp.ikonli.javafx.FontIcon;

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
    public static void setAvatar(
            byte[] imageBytes,
            ImageView imageView,
            FontIcon icon,
            double size
    ) {

        if (imageBytes != null && imageBytes.length > 0) {

            Image image = new Image(new ByteArrayInputStream(imageBytes));

            applyCoverCrop(imageView, image, size);

            Circle clip = new Circle(size / 2);
            clip.setCenterX(size / 2);
            clip.setCenterY(size / 2);

            imageView.setClip(clip);

            imageView.setVisible(true);
            icon.setVisible(false);

        } else {
            imageView.setVisible(false);
            icon.setVisible(true);
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
}