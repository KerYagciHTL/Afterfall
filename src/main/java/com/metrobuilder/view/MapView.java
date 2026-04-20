package com.metrobuilder.view;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

/**
 * Ein programmatisches Beispiel-View-Element für die Map.
 * Typischerweise wird das meiste in FXML gelöst, aber eigene
 * Controls (wie die Karte) können in solchen Klassen gekapselt werden.
 */
public class MapView extends Pane {

    public void drawStation(double x, double y, String name) {
        Circle stationCircle = new Circle(x, y, 10, Color.RED);
        this.getChildren().add(stationCircle);
    }
}
