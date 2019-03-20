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
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;

import static java.awt.Toolkit.getDefaultToolkit;

public class Main extends Application implements MouseListener {

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
            if (HoursData.getInstance().isOvertime()) {
                alert = new Alert(Alert.AlertType.CONFIRMATION, "Add overtime to hours?", ButtonType.YES, ButtonType.NO);
                res = alert.showAndWait();
                if (res.isPresent() && res.get() == ButtonType.YES) {
                    HoursData.getInstance().appendOvertime();
                }
            }
            HoursData.getInstance().save();
            SystemTray tray = SystemTray.getSystemTray();
            tray.remove(trayIcon);
        }
    }

    private void onMinimize(Boolean minimized) {
        if (minimized)
            stage.hide();
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
            trayIcon = new TrayIcon(image);
            trayIcon.setToolTip("PHours");

            // if the user double-clicks on the tray icon, show the main app stage.
            trayIcon.addMouseListener(this);

            // add the application tray icon to the system tray.
            tray.add(trayIcon);
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
        if (stage != null && !stage.isShowing()) {
            stage.show();
            stage.setAlwaysOnTop(true);
            stage.toFront();
            stage.setAlwaysOnTop(false);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            Platform.runLater(this::showStage);
            e.consume();
        } else {
            String message = HoursData.getInstance().getNotification();
            Notification.getInstance(this.trayIcon).show(message);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        System.out.println("mousePressed");
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // not implemented
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        System.out.println("mouseEntered");
    }

    @Override
    public void mouseExited(MouseEvent e) {
        System.out.println("mouseExited");
    }
}
