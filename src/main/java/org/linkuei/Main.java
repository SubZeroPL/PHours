package org.linkuei;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;

import static java.awt.Toolkit.getDefaultToolkit;

public class Main extends Application {

    private Stage stage;
    private TrayIcon trayIcon;

    public static void main(String[] args) {
        launch(args);
    }

    private void onClose(WindowEvent event) {
        var alert = new Alert(Alert.AlertType.CONFIRMATION, "Really close?", ButtonType.YES, ButtonType.NO);
        var res = alert.showAndWait();
        if (res.isPresent() && res.get().getButtonData().isCancelButton())
            event.consume();
        else {
            Platform.setImplicitExit(true);
            if (HoursData.getInstance().getCurrentTime().toSecondOfDay() != 0) {
                if (HoursData.getInstance().isOvertime()) {
                    alert = new Alert(Alert.AlertType.CONFIRMATION, "Add overtime to hours?", ButtonType.YES, ButtonType.NO);
                    res = alert.showAndWait();
                    if (res.isPresent() && res.get() == ButtonType.YES) {
                        HoursData.getInstance().appendOvertime();
                    }
                } else {
                    alert = new Alert(Alert.AlertType.CONFIRMATION, "Add time to hours?", ButtonType.YES, ButtonType.NO);
                    res = alert.showAndWait();
                    if (res.isPresent() && res.get() == ButtonType.YES) {
                        HoursData.getInstance().appendUndertime();
                    }
                }
            }
            HoursData.getInstance().save();
            SystemTray tray = SystemTray.getSystemTray();
            tray.remove(this.trayIcon);
        }
    }

    private void onMinimize(Boolean minimized) {
        if (minimized)
            this.stage.hide();
    }

    @Override
    public void start(Stage stage) throws Exception {
        this.stage = stage;
        Platform.setImplicitExit(false);

        HoursData.getInstance().load();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("mainForm.fxml"));
        Parent root = loader.load();
        ((Controller) loader.getController()).init();
        stage.setTitle("Job");
        stage.setOnCloseRequest(this::onClose);
        stage.iconifiedProperty().addListener((observable, oldValue, minimized) -> this.onMinimize(minimized));
        stage.getIcons().add(new Image(getClass().getResourceAsStream("icon_16.png")));
        stage.getIcons().add(new Image(getClass().getResourceAsStream("icon_48.png")));
        stage.setResizable(false);
        stage.setScene(new Scene(root));
        stage.show();

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
                System.out.println("No system tray support, application exiting.");
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
            Notification.getInstance(this.trayIcon);
        } catch (AWTException | IOException e) {
            System.out.println("Unable to init system tray");
            e.printStackTrace();
        }
    }

    /**
     * Shows the application stage and ensures that it is brought ot the front of all stages.
     */
    private void showStage() {
        if (this.stage != null && !this.stage.isShowing()) {
            this.stage.setAlwaysOnTop(true);
            this.stage.show();
            this.stage.toFront();
            this.stage.setAlwaysOnTop(false);
        }
    }

    private class MyMouseAdapter extends MouseAdapter {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                Platform.runLater(Main.this::showStage);
                e.consume();
            } else {
                String message = HoursData.getInstance().getNotification();
                Notification.getInstance(Main.this.trayIcon).show(message);
            }
        }
    }
}
