package org.linkuei.notifications;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class ToastController {
    @FXML
    Label toastText;

    private Stage stage;

    public void init(Stage stage) {
        this.stage = stage;
        toastText.setOnMouseClicked(event -> this.stage.hide());
    }

    /**
     * Method to show toast.
     */
    public void show() {
        this.stage.show();
    }

    /**
     * Method to set displayed text.
     *
     * @param text text to display
     */
    public void setText(String text) {
        this.toastText.setText(text);
    }
}
