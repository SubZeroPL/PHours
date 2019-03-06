package org.linkuei;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.util.Optional;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    private static void onClose(WindowEvent windowEvent) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setHeaderText("Really close?");
        Optional<ButtonType> res = alert.showAndWait();
        if (res.isPresent() && res.get().getButtonData().isCancelButton())
            windowEvent.consume();
        else {
            if (HoursData.getInstance().isOvertime()) {
                alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setHeaderText("Add overtime to hours?");
                res = alert.showAndWait();
                if (res.isPresent() && res.get() == ButtonType.OK) {
                    HoursData.getInstance().appendOvertime();
                }
            }
            HoursData.getInstance().save();
        }
    }

    @Override
    public void start(Stage stage) throws Exception {
        HoursData.getInstance().load();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("mainForm.fxml"));
        Parent root = loader.load();
        ((Controller) loader.getController()).init();
        stage.setTitle("Job");
        stage.setOnCloseRequest(Main::onClose);
        //stage.getIcons().add(null);
        stage.setResizable(false);
        stage.setScene(new Scene(root));
        stage.show();
    }
}
