package org.linkuei.notifications

import org.linkuei.HoursDataHandler
import org.linkuei.HoursDataHandler.currentTimeString
import org.linkuei.HoursDataHandler.endTimeString
import org.linkuei.HoursDataHandler.startTimeString
import java.awt.GraphicsEnvironment
import javax.swing.BorderFactory
import javax.swing.JEditorPane
import javax.swing.JWindow
import javax.swing.Timer
import javax.swing.border.BevelBorder
import kotlin.math.roundToInt

private const val PAD = 10

class NotificationWindow(private val position: Int, private val type: NotificationType = NotificationType.PROGRESS) : JWindow() {
    private val textPane: JEditorPane = JEditorPane()
    private val timer: Timer = Timer(0) { _ -> close() }

    init {
        val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
        this.setSize(200, 100)
        val y = ge.defaultScreenDevice.displayMode.height - ((this.height + PAD) * this.position)
        val x = ge.defaultScreenDevice.displayMode.width - (this.width + PAD)
        this.setLocation(x, y)
        this.isAlwaysOnTop = true
        this.textPane.isEditable = false
        this.textPane.contentType = "text/html"
        this.textPane.border = BorderFactory.createBevelBorder(BevelBorder.RAISED)
        this.textPane.addMouseListener(NotificationWindowMouseAdapter())
        this.add(this.textPane)
    }

    fun showNotification() {
        val data = HoursDataHandler.hoursData
        val percent = (HoursDataHandler.progress * 100).roundToInt()
        val text = "${if (data.isOvertime) "-" else ""}${data.currentTimeString} of ${data.workHours} [${percent}%]<br />" +
                "<b>Start time:</b> ${data.startTimeString}<br />" +
                "<b>End time:</b> ${data.endTimeString}"
        val tu = "<b>${data.timeUpMessage}</b>"
        this.textPane.text = if (this.type == NotificationType.PROGRESS) text else tu
        this.isVisible = true

        this.timer.initialDelay = (data.notificationDuration * 1000)
        this.timer.delay = 0
        this.timer.isRepeats = false
        this.timer.start()
    }

    fun close(force: Boolean = false) {
        this.timer.stop()
        if (force || this.type == NotificationType.PROGRESS ||
                (HoursDataHandler.hoursData.autohideEotNotification && this.type == NotificationType.END_OF_TIME)) {
            this.isVisible = false
            this.dispose()
            NotificationManager.remove()
        }
    }
}