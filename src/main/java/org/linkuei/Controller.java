package org.linkuei;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.linkuei.notifications.NotificationManager;

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
            if (HoursData.getInstance().getCurrentTime() == LocalTime.MIN)
                return;
            this.startTimers();
            HoursData.getInstance().restart();
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
                HoursData.getInstance().addHours(result.get());
                break;
            case 2:
                HoursData.getInstance().removeHours(result.get());
                break;
        }
        this.update();
    }

    void init(Stage mainStage) {
        this.update();
        this.mainStage = mainStage;
        this.timeUpMessage.setText(HoursData.getInstance().getTimeUpMessage());
        this.spinMinutes.getEditor().setText(String.valueOf(HoursData.getInstance().getNotificationMinutes()));
        this.spinWorkHours.getEditor().setText(String.valueOf(HoursData.getInstance().getWorkHours()));
        this.timeUpMessage.textProperty().addListener((observable, oldValue, newValue) -> this.timeUpMessageChanged(newValue));
        this.spinMinutes.valueProperty().addListener((observable, oldValue, newValue) -> this.spinMinutesChanged(newValue));
        this.spinWorkHours.valueProperty().addListener(((observable, oldValue, newValue) -> this.spinWorkHoursChanged(newValue)));
    }

    private void startTimers() {
        TimerTask task = new TimerTask(this);
        this.timer = new ScheduledThreadPoolExecutor(1);
        this.timer.scheduleAtFixedRate(task, 1, 1, TimeUnit.SECONDS);
        NotificationTask notificationTask = new NotificationTask();
        long minutes = HoursData.getInstance().getNotificationMinutes();
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

    private void timeUpMessageChanged(String newValue) {
        HoursData.getInstance().setTimeUpMessage(newValue);
    }

    private void spinMinutesChanged(int value) {
        HoursData.getInstance().setNotificationMinutes(value);
    }

    private void spinWorkHoursChanged(int value) {
        HoursData.getInstance().setWorkHours(value);
    }

    void update() {
        this.lblTime.setText(HoursData.getInstance().getCurrentTimeString());
        if (HoursData.getInstance().isOvertime())
            this.lblTime.setStyle("-fx-text-fill: red");
        else
            this.lblTime.setStyle(null);
        this.lblStatus.setText(HoursData.getInstance().getHoursString());
        if (HoursData.getInstance().isNegativeHours())
            this.lblStatus.setStyle("-fx-text-fill: red");
        else
            this.lblStatus.setStyle(null);
        this.progress.setProgress(HoursData.getInstance().getProgress());
        this.lblStartTime.setText(HoursData.getInstance().getStartTimeString());
        this.lblEndTime.setText(HoursData.getInstance().getEndTimeString());
        this.updateProgressBar();
        if (HoursData.getInstance().getCurrentTime() == LocalTime.MIN && timer != null && enabled) {
            LOG.info("End time");
            NotificationManager.INSTANCE.show(HoursData.getInstance().getTimeUpMessage());
        }
    }

    private void updateProgressBar() {
        long red = Math.round(this.progress.getProgress() * 255);
        var green = 255 - red;
        var blue = (this.progress.getProgress() > 0.5) ? green * 2 : red * 2;
        this.progress.setStyle(String.format("-fx-accent: RGB(%d, %d, %d)", red, green, blue));
    }

    public void miReset() {
        HoursData.getInstance().resetCurrentTime();
        update();
    }

    public void miSetStart() {
        TimeEntryDialog dialog = new TimeEntryDialog("Enter time (H:m):");
        dialog.initOwner(this.mainStage);
        Optional<LocalTime> result = dialog.showAndWait();
        result.ifPresent(localTime -> HoursData.getInstance().setStartTime(localTime));
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
