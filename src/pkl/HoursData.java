package pkl;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class HoursData implements Serializable {
    private static HoursData INSTANCE = null;

    private LocalTime maxTime = LocalTime.of(8, 0);
    private LocalTime currentTime;
    private LocalTime hours;
    private boolean overtime = false;

    private HoursData() {
        this.currentTime = LocalTime.ofSecondOfDay(0);
        this.hours = LocalTime.ofSecondOfDay(0);
    }

    public static HoursData getInstance() {
        if (INSTANCE == null)
            INSTANCE = new HoursData();
        return INSTANCE;
    }

    public LocalTime getCurrentTime() {
        return currentTime;
    }

    public HoursData setMaxTime(LocalTime maxTime) {
        this.maxTime = maxTime;
        this.currentTime = maxTime;
        return this;
    }

    public String getCurrentTimeString() {
        return currentTime.format(DateTimeFormatter.ISO_LOCAL_TIME);
    }

    public LocalTime getHours() {
        return hours;
    }

    public HoursData setHours(LocalTime hours) {
        this.hours = hours;
        return this;
    }

    public String getHoursString() {
        return this.hours.format(DateTimeFormatter.ofPattern("H'h' m'm'"));
    }

    public double getProgress() {
        return (double) currentTime.toSecondOfDay() / maxTime.toSecondOfDay();
    }

    public boolean isOvertime() {
        return this.overtime;
    }

    public void save() {
        try (FileOutputStream fileOutputStream = new FileOutputStream("hours.dat"); ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)) {
            if (this.overtime)
                resetCurrentTime();
            objectOutputStream.writeObject(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void load() {
        try (FileInputStream fileInputStream = new FileInputStream("hours.dat"); ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)) {
            INSTANCE = (HoursData) objectInputStream.readObject();
        } catch (InvalidClassException e) {
            try {
                Files.deleteIfExists(Path.of("hours.dat"));
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void addHours(LocalTime hours) {
        this.hours = this.hours.plusSeconds(hours.toSecondOfDay());
    }

    public void removeHours(LocalTime hours) throws IllegalArgumentException {
        if (hours.isAfter(this.hours))
            throw new IllegalArgumentException("Too much time to remove");
        this.hours = this.hours.minusSeconds(hours.toSecondOfDay());
    }

    public void clearHours() {
        this.hours = LocalTime.ofSecondOfDay(0);
    }

    public void resetCurrentTime() {
        this.currentTime = this.maxTime;
        this.overtime = false;
    }

    public void minusTime() {
        if (this.currentTime.toSecondOfDay() == 0)
            this.overtime = true;
        this.currentTime = this.overtime ? this.currentTime.plusSeconds(1) : this.currentTime.minusSeconds(1);
    }

    public void appendOvertime() {
        this.hours = this.hours.plusSeconds(currentTime.truncatedTo(ChronoUnit.MINUTES).toSecondOfDay());
    }
}
