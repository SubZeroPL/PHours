package pkl;

import javafx.application.Platform;

public class TimerTask implements Runnable {
    private Controller controller;

    public TimerTask(Controller controller) {
        this.controller = controller;
    }

    @Override
    public void run() {
        Platform.runLater(() ->  {
            HoursData.getInstance().minusTime();
            controller.update();
        });
    }
}
