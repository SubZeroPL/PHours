package org.linkuei;

import javafx.application.Platform;
import org.linkuei.notifications.NotificationManager;

class NotificationTask implements Runnable {
    @Override
    public void run() {
        Platform.runLater(() -> NotificationManager.INSTANCE.show(HoursData.getInstance().getNotification()));
    }
}
