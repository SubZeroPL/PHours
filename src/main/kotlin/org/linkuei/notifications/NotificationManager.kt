package org.linkuei.notifications

object NotificationManager {
    private val activeNotifications = Stack<NotificationWindow>()

    @JvmOverloads
    fun show(type: NotificationType = NotificationType.PROGRESS) {
        val win = NotificationWindow(activeNotifications.maxSize() + 1)
        activeNotifications.push(win)
        win.showNotification(type)
    }

    fun remove() {
        activeNotifications.pop()
    }

    fun size(): Int {
        return activeNotifications.size
    }

    fun clear() {
        for (win in activeNotifications) {
            win.close()
        }
    }
}