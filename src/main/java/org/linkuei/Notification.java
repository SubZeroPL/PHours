package org.linkuei;

import org.apache.commons.lang3.StringUtils;

import java.awt.*;

public class Notification {
    private static Notification ourInstance = null;
    private final TrayIcon icon;

    private Notification(TrayIcon icon) {
        this.icon = icon;
    }

    public static Notification getInstance(TrayIcon icon) {
        if (ourInstance == null)
            ourInstance = new Notification(icon);
        return ourInstance;
    }

    public static Notification getInstance() {
        return getInstance(null);
    }

    public void show(String text) {
        this.show("PHours", text);
    }

    public void show(String caption, String text) {
        if (StringUtils.isBlank(text))
            return;
        if (this.icon != null)
            this.icon.displayMessage(caption, text, TrayIcon.MessageType.INFO);
    }
}
