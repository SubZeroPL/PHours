package org.linkuei.notifications

import java.awt.GraphicsEnvironment
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.util.concurrent.ScheduledThreadPoolExecutor
import javax.swing.JEditorPane
import javax.swing.JWindow

class Notification : MouseAdapter() {
    class HideNotificationTask : Runnable {
        override fun run() {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }
    }

    private val win = JWindow()
    private val timer: ScheduledThreadPoolExecutor = ScheduledThreadPoolExecutor(1)

    fun show(text: String) {
        val ge = GraphicsEnvironment.getLocalGraphicsEnvironment()
        val height = ge.defaultScreenDevice.displayMode.height
        val width = ge.defaultScreenDevice.displayMode.width
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
    }

    override fun mouseClicked(e: MouseEvent?) {
        win.isVisible = false
        win.dispose()
    }
}