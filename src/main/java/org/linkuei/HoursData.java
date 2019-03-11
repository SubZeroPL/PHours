package org.linkuei;

import java.io.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

class HoursData implements Serializable {
    private static HoursData INSTANCE = null;

    private LocalTime maxTime = LocalTime.of(8, 0);
    private LocalTime currentTime;
    private LocalTime hours;
    private boolean overtime = false;
    private String timeUpMessage;
    private int notificationMinutes;

    private HoursData() {
        this.currentTime = LocalTime.ofSecondOfDay(0);
        this.hours = LocalTime.ofSecondOfDay(0);
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

    LocalTime getHours() {
        return hours;
    }

    String getHoursString() {
        return this.hours.format(DateTimeFormatter.ofPattern("H'h' m'm'"));
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

    void addHours(LocalTime hours) {
        this.hours = this.hours.plusSeconds(hours.toSecondOfDay());
    }

    void removeHours(LocalTime hours) throws IllegalArgumentException {
        if (hours.isAfter(this.hours))
            throw new IllegalArgumentException("Too much time to remove");
        this.hours = this.hours.minusSeconds(hours.toSecondOfDay());
    }

    void clearHours() {
        this.hours = LocalTime.ofSecondOfDay(0);
    }

    void resetCurrentTime() {
        this.currentTime = this.maxTime;
        this.overtime = false;
    }

    void minusTime() {
        if (this.currentTime.toSecondOfDay() == 0) {
            this.overtime = true;
        }
        this.currentTime = this.overtime ? this.currentTime.plusSeconds(1) : this.currentTime.minusSeconds(1);
    }

    void appendOvertime() {
        this.hours = this.hours.plusSeconds(currentTime.truncatedTo(ChronoUnit.MINUTES).toSecondOfDay());
    }

    String getNotification() {
        long percent = Math.round(getProgress() * 100);
        return String.format("%s of %s [%d%%]", getCurrentTimeString(), getMaxTimeString(), percent);
    }
}
