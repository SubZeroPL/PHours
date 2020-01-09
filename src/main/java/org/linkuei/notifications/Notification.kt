package org.linkuei.notifications

import java.awt.GraphicsEnvironment
import java.awt.event.MouseAdapter
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.logging.Logger
import javax.swing.JEditorPane
import javax.swing.JWindow

private const val TIMEOUT: Long = 5

// TODO create better notification management system so notification stacking is possible
class Notification : MouseAdapter() {
    private val log = Logger.getLogger(Notification::class.java.name)

    fun show(text: String) {
        log.entering(Notification::class.java.name, this.toString())
        val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
        val height = ge.defaultScreenDevice.displayMode.height
        val width = ge.defaultScreenDevice.displayMode.width
        val win = JWindow()
        win.setSize(200, 100)
        win.setLocation(width - (win.width + 10), height - (win.height + 10))
        win.isAlwaysOnTop = true
        win.addMouseListener(this)
        val pane = JEditorPane()
        pane.isEditable = false
        pane.contentType = "text/html"
        pane.text = text
        win.add(pane)
        win.isVisible = true
        val timer = ScheduledThreadPoolExecutor(1)
        timer.schedule(Runnable {
            log.info("Hide")
            win.isVisible = false
            win.dispose()
            timer.shutdownNow()
        }, TIMEOUT, TimeUnit.SECONDS)
    }
}