package org.linkuei;

import javafx.application.Platform;

class NotificationTask implements Runnable {
    @Override
    public void run() {
        Platform.runLater(() -> Notification.getInstance().show(HoursData.getInstance().getNotification()));
    }
}
