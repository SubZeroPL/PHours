package org.linkuei;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Controller {
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

    @FXML
    void btnStart() {
        if (enabled) {
            if (timer != null)
                timer.shutdown();
            if (notificationTimer != null)
                notificationTimer.shutdown();
            btnStart.setText(Labels.START.getText());
            enabled = false;
        } else {
            if (HoursData.getInstance().getCurrentTime() == LocalTime.MIN)
                return;
            TimerTask task = new TimerTask(this);
            timer = new ScheduledThreadPoolExecutor(1);
            timer.scheduleAtFixedRate(task, 1, 1, TimeUnit.SECONDS);
            NotificationTask notificationTask = new NotificationTask();
            long minutes = HoursData.getInstance().getNotificationMinutes();
            if (minutes > 0) {
                notificationTimer = new ScheduledThreadPoolExecutor(1);
                notificationTimer.scheduleAtFixedRate(notificationTask, minutes, minutes, TimeUnit.MINUTES);
            }
            HoursData.getInstance().recalculate();
            btnStart.setText(Labels.STOP.getText());
            enabled = true;
        }
    }

    @FXML
    void btnAdd(ActionEvent event) {
        var btn = (Button) event.getSource();
        var id = Integer.parseInt(String.valueOf(btn.getUserData()));
        var dialog = new TextInputDialog("1");
        dialog.setGraphic(null);
        dialog.setHeaderText(id == 1 ? "Time to add" : "Time to remove");
        Optional<String> result = dialog.showAndWait();
        if (result.isEmpty())
            return;
        try {
            var time = LocalTime.parse(result.get(), DateTimeFormatter.ofPattern("H[:m]"));
            switch (id) {
                case 1:
                    HoursData.getInstance().addHours(time);
                    break;
                case 2:
                    HoursData.getInstance().removeHours(time);
                    break;
            }
            update();
        } catch (NumberFormatException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Not a number");
            alert.show();
        } catch (NoSuchElementException e) {
            e.printStackTrace();
        }
    }

    void init() {
        this.update();
        this.timeUpMessage.setText(HoursData.getInstance().getTimeUpMessage());
        this.spinMinutes.getEditor().setText(String.valueOf(HoursData.getInstance().getNotificationMinutes()));
        this.spinWorkHours.getEditor().setText(String.valueOf(HoursData.getInstance().getWorkHours()));
        this.timeUpMessage.textProperty().addListener((observable, oldValue, newValue) -> this.timeUpMessageChanged(newValue));
        this.spinMinutes.valueProperty().addListener((observable, oldValue, newValue) -> this.spinMinutesChanged(newValue));
        this.spinWorkHours.valueProperty().addListener(((observable, oldValue, newValue) -> this.spinWorkHoursChanged(newValue)));
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
            Notification.getInstance().show(HoursData.getInstance().getTimeUpMessage());
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
