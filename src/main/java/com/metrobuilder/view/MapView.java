package com.metrobuilder.view;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class MapView extends Pane {

    public void drawStation(double x, double y, String name) {
        Circle stationCircle = new Circle(x, y, 10, Color.RED);
        this.getChildren().add(stationCircle);
    }
}
