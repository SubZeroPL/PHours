package org.linkuei;

import org.apache.commons.lang3.StringUtils;

import java.awt.*;

class Notification {
    private static Notification ourInstance = null;
    private final TrayIcon icon;

    private Notification(TrayIcon icon) {
        this.icon = icon;
    }

    static Notification getInstance(TrayIcon icon) {
        if (ourInstance == null)
            ourInstance = new Notification(icon);
        return ourInstance;
    }

    static Notification getInstance() {
        return getInstance(null);
    }

    void show(String text) {
        if (StringUtils.isBlank(text))
            return;
        if (this.icon != null)
            this.icon.displayMessage("PHours", text, TrayIcon.MessageType.INFO);
    }
}
