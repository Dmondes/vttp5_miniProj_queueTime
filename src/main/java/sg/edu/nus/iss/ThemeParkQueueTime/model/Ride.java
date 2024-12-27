package sg.edu.nus.iss.ThemeParkQueueTime.model;

import java.time.LocalDateTime;

public class Ride {
    private int id;
    private String name;
    private boolean isOpen;
    private int waitTime;
    private LocalDateTime lastUpdated;
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public boolean isOpen() {
        return isOpen;
    }
    public void setOpen(boolean isOpen) {
        this.isOpen = isOpen;
    }
    public int getWaitTime() {
        return waitTime;
    }
    public void setWaitTime(int waitTime) {
        this.waitTime = waitTime;
    }
    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }
    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
