package org.linkuei;

import javafx.application.Platform;
import org.linkuei.notifications.Notification;

class NotificationTask implements Runnable {
    @Override
    public void run() {
        Platform.runLater(() -> new Notification().show(HoursData.getInstance().getNotification()));
    }
}
