package org.linkuei;

import java.io.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

class HoursData implements Serializable {
    static final long serialVersionUID = 1L;

    private static HoursData INSTANCE = null;

    private LocalTime maxTime = LocalTime.of(8, 0);
    private LocalTime currentTime;
    private LocalTime hours;
    private boolean overtime = false;
    private boolean negativeHours;
    private String timeUpMessage;
    private int notificationMinutes;
    private LocalTime startTime;
    private LocalTime endTime;

    private HoursData() {
        this.currentTime = LocalTime.MIN;
        this.hours = LocalTime.MIN;
        this.startTime = LocalTime.MIN;
        this.endTime = LocalTime.MIN;
    }

    static HoursData getInstance() {
        if (INSTANCE == null)
            INSTANCE = new HoursData();
        return INSTANCE;
    }

    LocalTime getCurrentTime() {
        return currentTime;
    }

    void setMaxTime(LocalTime maxTime) {
        this.maxTime = maxTime;
        this.currentTime = maxTime;
    }

    private String getMaxTimeString() {
        return this.maxTime.format(DateTimeFormatter.ISO_LOCAL_TIME);
    }

    String getCurrentTimeString() {
        return currentTime.format(DateTimeFormatter.ISO_LOCAL_TIME);
    }

    String getHoursString() {
        return this.hours.format(DateTimeFormatter.ofPattern((this.negativeHours ? "-" : "") + "H'h' m'm'"));
    }

    double getProgress() {
        if (this.overtime)
            return 1.0;
        else
            return (double) currentTime.toSecondOfDay() / maxTime.toSecondOfDay();
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

    public void setStartTimeNow() {
        this.startTime = LocalTime.now();
    }

    String getStartTimeString() {
        return this.startTime.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    public void setEndTime() {
        if (this.startTime == LocalTime.MIN || this.currentTime == LocalTime.MIN)
            return;
        this.endTime = LocalTime.MIN;
        this.endTime = this.startTime.plusSeconds(this.currentTime.toSecondOfDay());
    }

    String getEndTimeString() {
        return this.endTime.format(DateTimeFormatter.ofPattern("HH:mm"));
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

    void addHours(LocalTime toAdd) {
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
    }

    void removeHours(LocalTime toRemove) throws IllegalArgumentException {
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
    }

    void resetCurrentTime() {
        this.currentTime = this.maxTime;
        this.overtime = false;
    }

    void minusTime() {
        if (this.currentTime == LocalTime.MIN) {
            this.overtime = true;
        }
        this.currentTime = this.overtime ? this.currentTime.plusSeconds(1) : this.currentTime.minusSeconds(1);
    }

    void appendOvertime() {
        this.removeHours(this.currentTime.truncatedTo(ChronoUnit.MINUTES));
    }

    void appendUndertime() {
        this.addHours(this.currentTime.truncatedTo(ChronoUnit.MINUTES));
    }

    String getNotification() {
        long percent = Math.round(this.getProgress() * 100);
        return String.format("%s%s of %s [%d%%]", this.isOvertime() ? "-" : "", this.getCurrentTimeString(), this.getMaxTimeString(), percent);
    }
}
