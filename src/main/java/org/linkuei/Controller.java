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
    private ScheduledThreadPoolExecutor timer, notificationTimer;
    private boolean enabled = false;

    @FXML
    void btnStart() {
        if (enabled) {
            enabled = false;
            timer.shutdown();
            notificationTimer.shutdown();
            btnStart.setText(Labels.START.getText());
        } else {
            if (HoursData.getInstance().getCurrentTime().toSecondOfDay() == 0)
                return;
            enabled = true;
            TimerTask task = new TimerTask(this);
            timer = new ScheduledThreadPoolExecutor(1);
            timer.scheduleAtFixedRate(task, 1, 1, TimeUnit.SECONDS);
            NotificationTask notificationTask = new NotificationTask();
            long minutes = HoursData.getInstance().getNotificationMinutes();
            notificationTimer = new ScheduledThreadPoolExecutor(1);
            notificationTimer.scheduleAtFixedRate(notificationTask, minutes, minutes, TimeUnit.MINUTES);
            btnStart.setText(Labels.STOP.getText());
        }
    }

    @FXML
    void btnAdd(ActionEvent event) {
        Button btn = (Button) event.getSource();
        int id = Integer.parseInt(String.valueOf(btn.getUserData()));
        TextInputDialog dialog = new TextInputDialog("1");
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
                case 2: {
                    try {
                        if (HoursData.getInstance().getHours().isBefore(time)) {
                            Alert q = new Alert(Alert.AlertType.CONFIRMATION);
                            q.setContentText("Too much time to remove. Reset time?");
                            Optional<ButtonType> r = q.showAndWait();
                            if (r.isPresent() && r.get() == ButtonType.OK)
                                HoursData.getInstance().clearHours();
                        } else
                            HoursData.getInstance().removeHours(time);
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    }
                }
                break;
            }
            lblStatus.setText(HoursData.getInstance().getHoursString());
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
        this.lblStatus.setText(HoursData.getInstance().getHoursString());
        this.lblTime.setText(HoursData.getInstance().getCurrentTimeString());
        this.timeUpMessage.setText(HoursData.getInstance().getTimeUpMessage());
        this.spinMinutes.getEditor().setText(String.valueOf(HoursData.getInstance().getNotificationMinutes()));
        this.timeUpMessage.textProperty().addListener((observable, oldValue, newValue) -> this.timeUpMessageChanged(newValue));
        this.spinMinutes.valueProperty().addListener((observable, oldValue, newValue) -> this.spinMinutesChanged(newValue));
    }

    private void timeUpMessageChanged(String newValue) {
        HoursData.getInstance().setTimeUpMessage(newValue);
    }

    private void spinMinutesChanged(Integer value) {
        HoursData.getInstance().setNotificationMinutes(value);
    }

    void update() {
        lblTime.setText(HoursData.getInstance().getCurrentTimeString());
        if (HoursData.getInstance().getCurrentTime().toSecondOfDay() == 0) {
            Notification.getInstance(null).show(HoursData.getInstance().getTimeUpMessage());
        }
        if (HoursData.getInstance().isOvertime())
            lblTime.setStyle("-fx-text-fill: red");
        else
            lblTime.setStyle(null);
        progress.setProgress(HoursData.getInstance().getProgress());
    }

    public void miSetTime(ActionEvent event) {
        MenuItem menuItem = (MenuItem) event.getSource();
        int pos = Integer.parseInt(String.valueOf(menuItem.getUserData()));
        if (pos != 0) {
            HoursData.getInstance().setMaxTime(LocalTime.of(pos, 0, 0));
        } else {
            TimeEntryDialog dialog = new TimeEntryDialog("Enter time (H:m):");
            Optional<LocalTime> result = dialog.showAndWait();
            HoursData.getInstance().setMaxTime(result.orElse(LocalTime.ofSecondOfDay(0)));
        }
        update();
    }

    public void miReset(ActionEvent event) {
        HoursData.getInstance().resetCurrentTime();
        update();
    }

    private enum Labels {
        START("Start"), STOP("Stop");

        private final String text;

        Labels(String text) {
            this.text = text;
        }

        String getText() {
            return this.text;
        }
    }
}
