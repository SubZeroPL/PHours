package org.linkuei;

import javafx.geometry.Pos;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

class TimeEntryDialog extends Dialog<LocalTime> implements Callback<ButtonType, LocalTime> {
    private final TextField textField;

    public TimeEntryDialog(String text) {
        super();
        this.textField = new TextField();
        Label label = new Label(text);
        label.getStyleClass().add("content");
        label.setWrapText(true);
        label.setPrefWidth(-1.0D);
        GridPane grid = new GridPane();
        grid.setHgap(10.0D);
        grid.setAlignment(Pos.CENTER_LEFT);
        grid.getChildren().clear();
        grid.add(label, 0, 0);
        grid.add(this.textField, 1, 0);
        this.getDialogPane().setContent(grid);
        this.getDialogPane().getButtonTypes().add(ButtonType.OK);
        this.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        this.setResultConverter(this);
    }

    @Override
    public LocalTime call(ButtonType buttonType) {
        if (buttonType == ButtonType.CANCEL)
            return null;
        return LocalTime.parse(this.textField.getText(), DateTimeFormatter.ofPattern("H:m"));
    }
}
