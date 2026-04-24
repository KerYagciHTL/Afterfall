package com.metrobuilder.view;

import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;

public class MapView extends Pane {

    public void placeSprite(WritableImage image, double x, double y, double size) {
        ImageView iv = new ImageView(image);
        iv.setFitWidth(size);
        iv.setFitHeight(size);
        iv.setPreserveRatio(true);
        iv.setLayoutX(x);
        iv.setLayoutY(y);
        getChildren().add(iv);
    }
}
