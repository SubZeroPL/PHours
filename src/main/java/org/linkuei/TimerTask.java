package org.linkuei;

import javafx.application.Platform;

class TimerTask implements Runnable {
    private final Controller controller;

    TimerTask(Controller controller) {
        this.controller = controller;
    }

    @Override
    public void run() {
        Platform.runLater(() -> {
            HoursDataHandler.INSTANCE.minusTime();
            controller.update();
        });
    }
}
