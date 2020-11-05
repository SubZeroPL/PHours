package org.linkuei;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.linkuei.notifications.NotificationManager;
import org.linkuei.notifications.NotificationType;

import java.time.LocalTime;
import java.util.Optional;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class Controller {
    private static final Logger LOG = Logger.getLogger(Controller.class.getName());

    @FXML
    Button btnStart;
    @FXML
    ProgressBar progress;
    @FXML
    Label lblTime;
    @FXML
    Label lblStatus;
    @FXML
    TextField timeUpMessage;
    @FXML
    Spinner<Integer> spinMinutes;
    @FXML
    Label lblStartTime;
    @FXML
    Label lblEndTime;
    @FXML
    Spinner<Integer> spinWorkHours;
    @FXML
    Spinner<Integer> spinSeconds;
    @FXML
    CheckBox cbEot;

    private ScheduledThreadPoolExecutor timer, notificationTimer;
    private boolean enabled = false;
    private Stage mainStage = null;

    @FXML
    void btnStartClick() {
        if (enabled) {
            this.stopTimers();
            btnStart.setText(Labels.START.getText());
            enabled = false;
        } else {
            if (HoursDataHandler.INSTANCE.getHoursData().getCurrentTime() == LocalTime.MIN)
                return;
            this.startTimers();
            HoursDataHandler.INSTANCE.restart();
            btnStart.setText(Labels.STOP.getText());
            enabled = true;
        }
    }

    @FXML
    void btnAddClick(ActionEvent event) {
        var btn = (Button) event.getSource();
        var id = Integer.parseInt(String.valueOf(btn.getUserData()));
        var dialog = new TimeEntryDialog(id == 1 ? "Time to add" : "Time to remove");
        dialog.initOwner(this.mainStage);
        Optional<LocalTime> result = dialog.showAndWait();
        if (result.isEmpty()) {
            event.consume();
            return;
        }
        switch (id) {
            case 1:
                HoursDataHandler.INSTANCE.addHours(result.get());
                break;
            case 2:
                HoursDataHandler.INSTANCE.removeHours(result.get());
                break;
        }
        this.update();
    }

    void init(Stage mainStage) {
        this.update();
        this.mainStage = mainStage;
        this.timeUpMessage.setText(HoursDataHandler.INSTANCE.getHoursData().getTimeUpMessage());
        this.spinMinutes.getEditor().setText(String.valueOf(HoursDataHandler.INSTANCE.getHoursData().getNotificationMinutes()));
        this.spinWorkHours.getEditor().setText(String.valueOf(HoursDataHandler.INSTANCE.getHoursData().getWorkHours()));
        this.timeUpMessage.textProperty().addListener((observable, oldValue, newValue) -> this.timeUpMessageChanged(newValue));
        this.spinMinutes.valueProperty().addListener((observable, oldValue, newValue) -> this.spinMinutesChanged(newValue));
        this.spinSeconds.getEditor().setText(String.valueOf(HoursDataHandler.INSTANCE.getHoursData().getNotificationDuration()));
        this.spinSeconds.valueProperty().addListener((observable, oldValue, newValue) -> this.spinSecondsChanged(newValue));
        this.spinWorkHours.valueProperty().addListener(((observable, oldValue, newValue) -> this.spinWorkHoursChanged(newValue)));
        this.cbEot.setSelected(HoursDataHandler.INSTANCE.getHoursData().getAutohideEotNotification());
        this.cbEot.selectedProperty().addListener((observable, oldValue, newValue) -> this.cbEotChanged(newValue));
    }

    private void startTimers() {
        TimerTask task = new TimerTask(this);
        this.timer = new ScheduledThreadPoolExecutor(1);
        this.timer.scheduleAtFixedRate(task, 1, 1, TimeUnit.SECONDS);
        NotificationTask notificationTask = new NotificationTask();
        long minutes = HoursDataHandler.INSTANCE.getHoursData().getNotificationMinutes();
        if (minutes > 0) {
            this.notificationTimer = new ScheduledThreadPoolExecutor(1);
            this.notificationTimer.scheduleAtFixedRate(notificationTask, minutes, minutes, TimeUnit.MINUTES);
        }
    }

    public void stopTimers() {
        if (this.timer != null) {
            this.timer.shutdown();
            this.timer = null;
        }
        if (this.notificationTimer != null) {
            this.notificationTimer.shutdown();
            this.notificationTimer = null;
        }
    }

    private void spinSecondsChanged(int value) {
        HoursDataHandler.INSTANCE.getHoursData().setNotificationDuration(value);
    }

    private void cbEotChanged(boolean value) {
        HoursDataHandler.INSTANCE.getHoursData().setAutohideEotNotification(value);
    }

    private void timeUpMessageChanged(String newValue) {
        HoursDataHandler.INSTANCE.getHoursData().setTimeUpMessage(newValue);
    }

    private void spinMinutesChanged(int value) {
        HoursDataHandler.INSTANCE.getHoursData().setNotificationMinutes(value);
    }

    private void spinWorkHoursChanged(int value) {
        HoursDataHandler.INSTANCE.getHoursData().setWorkHours(value);
    }

    void update() {
        this.lblTime.setText(HoursDataHandler.INSTANCE.getCurrentTimeString(HoursDataHandler.INSTANCE.getHoursData()));
        if (HoursDataHandler.INSTANCE.getHoursData().isOvertime())
            this.lblTime.setStyle("-fx-text-fill: red");
        else
            this.lblTime.setStyle(null);
        this.lblStatus.setText(HoursDataHandler.INSTANCE.getHoursString(HoursDataHandler.INSTANCE.getHoursData()));
        if (HoursDataHandler.INSTANCE.getHoursData().isNegativeHours())
            this.lblStatus.setStyle("-fx-text-fill: red");
        else
            this.lblStatus.setStyle(null);
        this.progress.setProgress(HoursDataHandler.INSTANCE.getProgress());
        this.lblStartTime.setText(HoursDataHandler.INSTANCE.getStartTimeString());
        this.lblEndTime.setText(HoursDataHandler.INSTANCE.getEndTimeString());
        this.updateProgressBar();
        if (HoursDataHandler.INSTANCE.getHoursData().getCurrentTime() == LocalTime.MIN && timer != null && enabled) {
            LOG.info("End time");
            NotificationManager.INSTANCE.show(NotificationType.END_OF_TIME);
        }
    }

    private void updateProgressBar() {
        long red = Math.round(this.progress.getProgress() * 255);
        var green = 255 - red;
        var blue = (this.progress.getProgress() > 0.5) ? green * 2 : red * 2;
        this.progress.setStyle(String.format("-fx-accent: RGB(%d, %d, %d)", red, green, blue));
    }

    public void miReset() {
        HoursDataHandler.INSTANCE.resetCurrentTime();
        update();
    }

    public void miSetStart() {
        TimeEntryDialog dialog = new TimeEntryDialog("Enter time (H:m):");
        dialog.initOwner(this.mainStage);
        Optional<LocalTime> result = dialog.showAndWait();
        result.ifPresent(localTime -> HoursDataHandler.INSTANCE.setStartTime(localTime));
        update();
    }

    private enum Labels {
        START("_Start"), STOP("S_top");

        private final String text;

        Labels(String text) {
            this.text = text;
        }

        String getText() {
            return this.text;
        }
    }
}
