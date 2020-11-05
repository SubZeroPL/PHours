package org.linkuei

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import java.io.File
import java.io.IOException
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.logging.Level
import java.util.logging.Logger

object HoursDataHandler {
    private val LOG = Logger.getLogger(HoursDataHandler::class.java.name)

    val hoursData: HoursData

    init {
        hoursData = load()
    }

    val progress: Double
        get() = if (this.hoursData.isOvertime) 1.0 else {
            this.hoursData.currentTime.toSecondOfDay().toDouble() / LocalTime.of(this.hoursData.workHours, 0).toSecondOfDay()
        }

    val HoursData.currentTimeString: String
        get() = currentTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"))

    val HoursData.hoursString: String
        get() = hours.format(DateTimeFormatter.ofPattern((if (isNegativeHours) "-" else "") + "H'h' m'm'"))

    val HoursData.startTimeString: String
        get() = startTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"))

    val HoursData.endTimeString: String
        get() = endTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"))

    fun setStartTime(startTime: LocalTime) {
        this.hoursData.startTime = startTime
        recalculate()
        this.hoursData.endTime = this.hoursData.startTime.plusHours(LocalTime.of(this.hoursData.workHours, 0).hour.toLong())
    }

    fun recalculate() {
        val rest = LocalTime.now().minusSeconds(this.hoursData.startTime.toSecondOfDay().toLong()).truncatedTo(ChronoUnit.SECONDS)
        val workHoursTime = LocalTime.of(this.hoursData.workHours, 0)
        if (workHoursTime >= rest) {
            this.hoursData.currentTime = workHoursTime.minusSeconds(rest.toSecondOfDay().toLong())
            this.hoursData.isOvertime = false
        } else {
            this.hoursData.currentTime = rest.minusSeconds(workHoursTime.toSecondOfDay().toLong())
            this.hoursData.isOvertime = true
        }
    }

    fun restart() {
        this.hoursData.endTime = if (!this.hoursData.isOvertime) {
            LocalTime.now().plusSeconds(this.hoursData.currentTime.toSecondOfDay().toLong())
        } else {
            LocalTime.now().minusSeconds(this.hoursData.currentTime.toSecondOfDay().toLong())
        }
    }

    val startTimeString: String
        get() = this.hoursData.startTime.format(DateTimeFormatter.ofPattern("HH:mm"))
    val endTimeString: String
        get() = this.hoursData.endTime.format(DateTimeFormatter.ofPattern("HH:mm"))

    fun save() {
        try {
            if (this.hoursData.isOvertime) resetCurrentTime()
            val str = Gson().toJson(this.hoursData)
            File("hours.json").writeText(str)
        } catch (e: IOException) {
            LOG.log(Level.SEVERE, "Problem saving configuration file", e)
        }
    }

    private fun load(): HoursData {
        var data: HoursData? = null
        try {
            val str = File("hours.json").readText()
            data = Gson().fromJson(str, HoursData::class.java)
            if (data == null) {
                LOG.info("Configuration file could not be read, new will be created")
            }
        } catch (e: IOException) {
            LOG.info("Configuration file not found, new will be created")
        } catch (e: JsonSyntaxException) {
            LOG.log(Level.WARNING, "Problem loading configuration file, the file will be recreated", e)
        }
        return data ?: HoursData()
    }

    fun addHours(toAdd: LocalTime) {
        if (!this.hoursData.isNegativeHours) {
            this.hoursData.hours = this.hoursData.hours.plusSeconds(toAdd.toSecondOfDay().toLong())
        } else {
            if (toAdd.isAfter(this.hoursData.hours)) {
                this.hoursData.hours = toAdd.minusSeconds(this.hoursData.hours.toSecondOfDay().toLong())
                this.hoursData.isNegativeHours = false
            } else {
                this.hoursData.hours = this.hoursData.hours.minusSeconds(toAdd.toSecondOfDay().toLong())
            }
        }
        if (this.hoursData.hours.toSecondOfDay() == 0) this.hoursData.isNegativeHours = false
    }

    fun removeHours(toRemove: LocalTime) {
        if (this.hoursData.isNegativeHours) this.hoursData.hours = this.hoursData.hours.plusSeconds(toRemove.toSecondOfDay().toLong()) else {
            if (toRemove.isAfter(this.hoursData.hours)) {
                this.hoursData.hours = toRemove.minusSeconds(this.hoursData.hours.toSecondOfDay().toLong())
                this.hoursData.isNegativeHours = true
            } else {
                this.hoursData.hours = this.hoursData.hours.minusSeconds(toRemove.toSecondOfDay().toLong())
            }
        }
        if (this.hoursData.hours.toSecondOfDay() == 0) this.hoursData.isNegativeHours = false
    }

    fun resetCurrentTime() {
        this.hoursData.currentTime = LocalTime.of(this.hoursData.workHours, 0)
        this.hoursData.isOvertime = false
    }

    fun minusTime() {
        if (this.hoursData.currentTime === LocalTime.MIN) {
            this.hoursData.isOvertime = true
        }
        this.hoursData.currentTime = if (this.hoursData.isOvertime) this.hoursData.currentTime.plusSeconds(1) else this.hoursData.currentTime.minusSeconds(1)
    }

    fun appendOvertime() {
        addHours(this.hoursData.currentTime.truncatedTo(ChronoUnit.MINUTES))
    }

    fun appendUndertime() {
        removeHours(this.hoursData.currentTime.truncatedTo(ChronoUnit.MINUTES))
    }
}