package org.linkuei.notifications;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Toast {
    private static final Logger LOG = Logger.getLogger(Toast.class.getName());
    private static Toast INSTANCE;

    static {
        try {
            INSTANCE = new Toast();
        } catch (IOException e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private Stage stage;
    private ToastController controller;

    public Toast() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("toast.fxml"));
        Parent root = loader.load();
        this.controller = loader.getController();
        this.stage = new Stage(StageStyle.UNDECORATED);
        this.stage.setScene(new Scene(root));
        this.stage.setX(100);
        this.stage.setY(100);
        this.controller.init(this.stage);
    }

    /**
     * Method to show toast.
     *
     * @param text text to show
     */
    public static void show(String text) {
        INSTANCE.controller.setText(text);
        INSTANCE.controller.show();
    }
}
