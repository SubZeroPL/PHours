package org.linkuei.notifications

import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JEditorPane

class NotificationWindowMouseAdapter : MouseAdapter() {
    override fun mouseClicked(e: MouseEvent?) {
        if (e?.component is JEditorPane) {
            val win = (e.component as JEditorPane).topLevelAncestor
            if (win is NotificationWindow) {
                win.close(true)
            }
        }
    }
}