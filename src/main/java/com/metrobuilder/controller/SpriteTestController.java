package com.metrobuilder.controller;

import com.metrobuilder.view.GameSprites;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.util.Pair;

import java.util.List;

/**
 * Test controller that renders all named sprites from GameSprites
 * so the sprite sheet import can be visually verified.
 */
public class SpriteTestController {

    @FXML
    private VBox content;

    private static final int DISPLAY_SIZE = 128;

    @FXML
    public void initialize() {
        GameSprites s = new GameSprites();

        addSection("Rails", List.of(
            new Pair<>("horizontal", s.railHorizontal()),
            new Pair<>("vertical",   s.railVertical())
        ));

        addSection("Train (Down) — Blue", List.of(
            new Pair<>("front", s.trainDownBlueFront()),
            new Pair<>("rear",  s.trainDownBlueRear())
        ));

        addSection("Train (Down) — Orange", List.of(
            new Pair<>("front", s.trainDownOrangeFront()),
            new Pair<>("rear",  s.trainDownOrangeRear())
        ));

        addSection("Train (Down) — Red", List.of(
            new Pair<>("front", s.trainDownRedFront()),
            new Pair<>("rear",  s.trainDownRedRear())
        ));

        addSection("Train (Left) — Blue", List.of(
            new Pair<>("front", s.trainLeftBlueFront()),
            new Pair<>("wagon", s.trainLeftBlueWagon()),
            new Pair<>("rear",  s.trainLeftBlueRear())
        ));

        addSection("Train (Left) — Orange", List.of(
            new Pair<>("front", s.trainLeftOrangeFront()),
            new Pair<>("wagon", s.trainLeftOrangeWagon()),
            new Pair<>("rear",  s.trainLeftOrangeRear())
        ));
    }

    private void addSection(String title, List<Pair<String, WritableImage>> sprites) {
        Label heading = new Label(title);
        heading.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #e0e0e0;");

        FlowPane flow = new FlowPane(12, 12);

        for (Pair<String, WritableImage> entry : sprites) {
            flow.getChildren().add(makeTile(entry.getKey(), entry.getValue()));
        }

        content.getChildren().addAll(heading, flow);
    }

    private VBox makeTile(String name, WritableImage image) {
        Label label = new Label(name);
        label.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 11px;");

        ImageView iv = new ImageView(image);
        iv.setFitWidth(DISPLAY_SIZE);
        iv.setFitHeight(DISPLAY_SIZE);
        iv.setPreserveRatio(true);

        VBox tile = new VBox(6, label, iv);
        tile.setStyle("-fx-background-color: #2d2d2d; -fx-padding: 10; -fx-background-radius: 6;");
        return tile;
    }
}
