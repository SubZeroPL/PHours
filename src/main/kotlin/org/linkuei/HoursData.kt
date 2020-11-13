package org.linkuei

import java.time.LocalTime

/**
 * currentTime - Current worktime
 * hours - Hours worked extra (displayed positive) or hours to make up (displayed negative)
 * isOvertime - Are we currently working in overtime
 * isNegativeHours
 * timeUpMessage
 * notificationMinutes - How often to show notifications in minutes
 * notificationDuration - For how long to show notification in seconds
 * autohideEotNotification - Auto-hide end-of-time notification
 * startTime
 * endTime
 */
data class HoursData(var workHours: Int = 8,
                     var currentTime: LocalTime = LocalTime.MIN,
                     var hours: LocalTime = LocalTime.MIN,
                     var isOvertime: Boolean = false,
                     var isNegativeHours: Boolean = false,
                     var timeUpMessage: String = "Time up",
                     var notificationMinutes: Int = 30,
                     var notificationDuration: Int = 5,
                     var autohideEotNotification: Boolean = true,
                     var startTime: LocalTime = LocalTime.MIN,
                     var endTime: LocalTime = LocalTime.MIN)