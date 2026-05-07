package at.htl.afterfall.controller;

import at.htl.afterfall.model.GameModel;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.input.KeyEvent;

public class MainController {

    @FXML private Label balanceLabel;
    @FXML private Label satisfactionLabel;
    @FXML private Label netWorthLabel;

    private final GameModel model = new GameModel();

    @FXML
    public void initialize() {
        balanceLabel.textProperty().bind(
            model.balanceProperty().asString("Kontostand: %.2f €")
        );
        satisfactionLabel.textProperty().bind(
            model.satisfactionProperty().asString("Zufriedenheit: %.0f %%")
        );
        netWorthLabel.textProperty().bind(
            model.netWorthProperty().asString("Net Worth: %.0f")
        );
    }

    @FXML
    public void onKeyPressed(KeyEvent event) {
        switch (event.getCode()) {
            case SPACE  -> System.out.println("Pause toggled");
            case ESCAPE -> System.out.println("Build mode cancelled");
            default     -> {}
        }
    }
}
