package org.linkuei.notifications

object NotificationManager {
    private val activeNotifications = Stack<NotificationWindow>()
    private val eotNotifications = Stack<NotificationWindow>()

    @JvmOverloads
    fun show(type: NotificationType = NotificationType.PROGRESS) {
        if (type == NotificationType.END_OF_TIME) {
            val win = NotificationWindow(1, type)
            eotNotifications.push(win)
            win.showNotification()
        } else {
            val win = NotificationWindow(activeNotifications.maxSize() + 1, type)
            activeNotifications.push(win)
            win.showNotification()
        }
    }

    fun remove(type: NotificationType = NotificationType.PROGRESS) {
        if (type == NotificationType.END_OF_TIME) eotNotifications.pop() else activeNotifications.pop()
    }

    fun clear() {
        for (win in activeNotifications) {
            win.close(true)
        }
        for (win in eotNotifications) {
            win.close(true)
        }
    }
}