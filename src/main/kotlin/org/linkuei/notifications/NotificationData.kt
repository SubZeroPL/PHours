package org.linkuei.notifications

import java.time.LocalTime

data class NotificationData(val currentTime: LocalTime, val workhours: Int, val progress: Double,
                            val startTime: LocalTime, val endTime: LocalTime, val overtime: Boolean,
                            val timeUpMessage: String)