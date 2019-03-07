package org.linkuei;

import javafx.application.Platform;

public class TimerTask implements Runnable {
    private Controller controller;

    TimerTask(Controller controller) {
        this.controller = controller;
    }

    @Override
    public void run() {
        Platform.runLater(() -> {
            HoursData.getInstance().minusTime();
            controller.update();
        });
    }
}
