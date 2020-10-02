package org.linkuei.notifications

object NotificationManager {
    private val activeNotifications = Stack<NotificationWindow>()

    fun show(text: String) {
        val win = NotificationWindow(activeNotifications.maxSize() + 1)
        activeNotifications.push(win)
        win.show(text)
    }

    fun remove() {
        activeNotifications.pop()
    }

    fun size(): Int {
        return activeNotifications.size
    }
}