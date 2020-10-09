package org.linkuei.notifications

import org.linkuei.HoursData
import java.awt.GraphicsEnvironment
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit
import javax.swing.BorderFactory
import javax.swing.JEditorPane
import javax.swing.JWindow
import javax.swing.border.BevelBorder
import kotlin.math.roundToInt

private const val TIMEOUT: Long = 5
private const val PAD = 10

class NotificationWindow(private val position: Int) : JWindow() {
    private val textPane: JEditorPane = JEditorPane()
    private val timer: ScheduledThreadPoolExecutor = ScheduledThreadPoolExecutor(1)

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

    fun showNotification(type: NotificationType) {
        val data = HoursData.getInstance().notificationData
        val percent = (data.progress * 100).roundToInt()
        val text = String.format(
                "%s%s of %s [%d%%]<br />" +
                        "<b>Start time:</b> %s<br />" +
                        "<b>End time:</b> %s",
                if (data.overtime) "-" else "", data.currentTime, data.workhours, percent, data.startTime, data.endTime)
        this.textPane.text = if (type == NotificationType.PROGRESS) text else data.timeUpMessage
        this.isVisible = true
        this.timer.schedule({
            close()
        }, TIMEOUT, TimeUnit.SECONDS)
    }

    fun close() {
        this.timer.shutdownNow()
        this.isVisible = false
        this.dispose()
        NotificationManager.remove()
    }
}