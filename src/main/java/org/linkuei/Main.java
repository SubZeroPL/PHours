package org.linkuei;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.linkuei.notifications.Notification;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.awt.Toolkit.getDefaultToolkit;

public class Main extends Application {
    private static final Logger LOG = Logger.getLogger(Main.class.getName());

    private Stage stage;
    private TrayIcon trayIcon;
    private double x, y;
    private Controller controller;

    public static void main(String[] args) {
        launch(args);
    }

    private void onClose(WindowEvent event) {
        var alert = new Alert(Alert.AlertType.CONFIRMATION, "Really close?", ButtonType.YES, ButtonType.NO);
        alert.initOwner(this.stage);
        var res = alert.showAndWait();
        if (res.isPresent() && res.get().getButtonData().isCancelButton())
            event.consume();
        else {
            Platform.setImplicitExit(true);
            if (HoursData.getInstance().getCurrentTime().toSecondOfDay() != 0) {
                if (HoursData.getInstance().isOvertime()) {
                    alert = new Alert(Alert.AlertType.CONFIRMATION, "Add overtime to hours?", ButtonType.YES, ButtonType.NO);
                    alert.initOwner(this.stage);
                    res = alert.showAndWait();
                    if (res.isPresent() && res.get() == ButtonType.YES) {
                        HoursData.getInstance().appendOvertime();
                    }
                } else {
                    alert = new Alert(Alert.AlertType.CONFIRMATION, "Add time to hours?", ButtonType.YES, ButtonType.NO);
                    alert.initOwner(this.stage);
                    res = alert.showAndWait();
                    if (res.isPresent() && res.get() == ButtonType.YES) {
                        HoursData.getInstance().appendUndertime();
                    }
                }
            }
            HoursData.getInstance().save();
            this.controller.stopTimers();
            SystemTray tray = SystemTray.getSystemTray();
            tray.remove(this.trayIcon);
        }
    }

    /**
     * Called when application is being minimized to icon
     *
     * @param minimized is application minimizing
     */
    private void onMinimize(boolean minimized) {
        if (minimized) {
            this.x = this.stage.getX();
            this.y = this.stage.getY();
            this.stage.setIconified(true);
            this.stage.hide();
        }
    }

    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;
        Platform.setImplicitExit(false);

        HoursData.getInstance().load();

        var bounds = Screen.getPrimary().getVisualBounds();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("mainForm.fxml"));
        Parent root = loader.load();
        this.controller = loader.getController();
        this.controller.init(this.stage);
        this.stage.setTitle("Job");
        this.stage.setOnCloseRequest(this::onClose);
        this.stage.iconifiedProperty().addListener((observable, oldValue, minimized) -> this.onMinimize(minimized));
        this.stage.getIcons().add(new Image(getClass().getResourceAsStream("icon_16.png")));
        this.stage.getIcons().add(new Image(getClass().getResourceAsStream("icon_48.png")));
        this.stage.setResizable(false);
        this.stage.setScene(new Scene(root));
        this.stage.show();

        this.x = (bounds.getWidth() - this.stage.getWidth()) / 2;
        this.y = (bounds.getHeight() - this.stage.getHeight()) / 2;
        this.stage.setX(this.x);
        this.stage.setY(this.y);

        // sets up the tray icon (using awt code run on the swing thread).
        SwingUtilities.invokeLater(this::addAppToTray);
    }

    /**
     * Sets up a system tray icon for the application.
     */
    private void addAppToTray() {
        try {
            // ensure awt toolkit is initialized.
            getDefaultToolkit();

            // app requires system tray support, just exit if there is no support.
            if (!SystemTray.isSupported()) {
                LOG.warning("No system tray support, application exiting.");
                Platform.exit();
            }

            // set up a system tray icon.
            SystemTray tray = SystemTray.getSystemTray();
            java.awt.Image image = ImageIO.read(getClass().getResource("icon_16.png"));
            this.trayIcon = new TrayIcon(image);
            this.trayIcon.setToolTip("PHours");

            // if the user double-clicks on the tray icon, show the main app stage.
            this.trayIcon.addMouseListener(new MyMouseAdapter());

            // add the application tray icon to the system tray.
            tray.add(this.trayIcon);
        } catch (AWTException | IOException e) {
            LOG.log(Level.WARNING, "Unable to init system tray", e);
        }
    }

    /**
     * Shows the application stage and ensures that it is brought ot the front of all stages.
     */
    private void showStage() {
        if (this.stage != null && !this.stage.isShowing()) {
            this.stage.setX(this.x);
            this.stage.setY(this.y);
            this.stage.show();
            this.stage.setIconified(false);
            this.stage.toFront();
        }
    }

    /**
     * Hides application stage
     */
    private void hideStage() {
        this.onMinimize(true);
    }

    /**
     * Mouse adapter implementation for tray icon clicks
     */
    private class MyMouseAdapter extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                if (stage.isIconified())
                    Platform.runLater(Main.this::showStage);
                else
                    Platform.runLater(Main.this::hideStage);
                e.consume();
            } else {
                String message = HoursData.getInstance().getNotification();
                Notification notification = new Notification();
                notification.show(message);
                // Notification.getInstance().show(message);
            }
        }
    }
}
