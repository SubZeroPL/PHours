package org.linkuei;

import java.io.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class HoursData implements Serializable {
    static final long serialVersionUID = 1L;

    private static HoursData INSTANCE = null;

    /**
     * Current worktime
     */
    private LocalTime currentTime;
    /**
     * Hours worked extra (displayed positive) or hours to make up (displayed negative)
     */
    private LocalTime hours;
    /**
     * Are we currently working in overtime
     */
    private boolean overtime = false;
    private boolean negativeHours;
    private String timeUpMessage;
    /**
     * How often to show notifications
     */
    private int notificationMinutes;
    private LocalTime startTime;
    private LocalTime endTime;
    private int workHours;

    private HoursData() {
        this.currentTime = LocalTime.MIN;
        this.hours = LocalTime.MIN;
        this.startTime = LocalTime.MIN;
        this.endTime = LocalTime.MIN;
    }

    public static HoursData getInstance() {
        if (INSTANCE == null)
            INSTANCE = new HoursData();
        return INSTANCE;
    }

    public LocalTime getCurrentTime() {
        return currentTime;
    }

    public String getCurrentTimeString() {
        return currentTime.format(DateTimeFormatter.ISO_LOCAL_TIME);
    }

    public String getHoursString() {
        return this.hours.format(DateTimeFormatter.ofPattern((this.negativeHours ? "-" : "") + "H'h' m'm'"));
    }

    double getProgress() {
        if (this.overtime)
            return 1.0;
        else {
            var workHoursTime = LocalTime.of(this.workHours, 0);
            return (double) this.currentTime.toSecondOfDay() / workHoursTime.toSecondOfDay();
        }
    }

    boolean isOvertime() {
        return this.overtime;
    }

    boolean isNegativeHours() {
        return this.negativeHours;
    }

    String getTimeUpMessage() {
        return this.timeUpMessage;
    }

    void setTimeUpMessage(String message) {
        this.timeUpMessage = message;
    }

    int getNotificationMinutes() {
        return this.notificationMinutes;
    }

    void setNotificationMinutes(int notificationMinutes) {
        this.notificationMinutes = notificationMinutes;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
        recalculate();
        var workHoursTime = LocalTime.of(this.workHours, 0);
        this.endTime = this.startTime.plusHours(workHoursTime.getHour());
    }

    public void recalculate() {
        var rest = LocalTime.now().minusSeconds(this.startTime.toSecondOfDay()).truncatedTo(ChronoUnit.SECONDS);
        var workHoursTime = LocalTime.of(this.workHours, 0);
        if (workHoursTime.compareTo(rest) >= 0) {
            this.currentTime = workHoursTime.minusSeconds(rest.toSecondOfDay());
            this.overtime = false;
        } else {
            this.currentTime = rest.minusSeconds(workHoursTime.toSecondOfDay());
            this.overtime = true;
        }
    }

    public void restart() {
        if (!this.overtime) {
            this.endTime = LocalTime.now().plusSeconds(this.currentTime.toSecondOfDay());
        } else {
            this.endTime = LocalTime.now().minusSeconds(this.currentTime.toSecondOfDay());
        }
    }

    String getStartTimeString() {
        return this.startTime.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    public LocalTime getEndTime() {
        return this.endTime;
    }

    String getEndTimeString() {
        return this.endTime.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    public int getWorkHours() {
        return this.workHours;
    }

    public void setWorkHours(int workHours) {
        this.workHours = workHours;
    }

    private String getWorkHoursString() {
        return String.format("%02d:00:00", this.workHours);
    }

    void save() {
        try (FileOutputStream fileOutputStream = new FileOutputStream("hours.dat"); ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)) {
            if (this.overtime)
                resetCurrentTime();
            objectOutputStream.writeObject(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void load() {
        try (FileInputStream fileInputStream = new FileInputStream("hours.dat"); ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)) {
            INSTANCE = (HoursData) objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void addHours(LocalTime toAdd) {
        if (!this.negativeHours) {
            this.hours = this.hours.plusSeconds(toAdd.toSecondOfDay());
        } else {
            if (toAdd.isAfter(this.hours)) {
                this.hours = toAdd.minusSeconds(this.hours.toSecondOfDay());
                this.negativeHours = false;
            } else {
                this.hours = this.hours.minusSeconds(toAdd.toSecondOfDay());
            }
        }
        if (this.hours.toSecondOfDay() == 0)
            this.negativeHours = false;
    }

    public void removeHours(LocalTime toRemove) throws IllegalArgumentException {
        if (this.negativeHours)
            this.hours = this.hours.plusSeconds(toRemove.toSecondOfDay());
        else {
            if (toRemove.isAfter(this.hours)) {
                this.hours = toRemove.minusSeconds(this.hours.toSecondOfDay());
                this.negativeHours = true;
            } else {
                this.hours = this.hours.minusSeconds(toRemove.toSecondOfDay());
            }
        }
        if (this.hours.toSecondOfDay() == 0)
            this.negativeHours = false;
    }

    public void resetHours() {
        this.hours = LocalTime.MIN;
    }

    public void resetCurrentTime() {
        this.currentTime = LocalTime.of(this.workHours, 0);
        this.overtime = false;
    }

    public void minusTime() {
        if (this.currentTime == LocalTime.MIN) {
            this.overtime = true;
        }
        this.currentTime = this.overtime ? this.currentTime.plusSeconds(1) : this.currentTime.minusSeconds(1);
    }

    public void appendOvertime() {
        this.addHours(this.currentTime.truncatedTo(ChronoUnit.MINUTES));
    }

    public void appendUndertime() {
        this.removeHours(this.currentTime.truncatedTo(ChronoUnit.MINUTES));
    }

    String getNotification() {
        long percent = Math.round(this.getProgress() * 100);
        return String.format("%s%s of %s [%d%%]\nStart time: %s\nEnd time: %s", this.isOvertime() ? "-" : "", this.getCurrentTimeString(), this.getWorkHoursString(), percent, getStartTimeString(), getEndTimeString());
    }
}
