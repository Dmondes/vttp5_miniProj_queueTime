package sg.edu.nus.iss.ThemeParkQueueTime.model;

import java.util.List;

public class RideRide {

    private int id;
    private String name;
    private List<Ride> rides;

    // Getters and Setters
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

    public List<Ride> getRides() {
        return rides;
    }

    public void setRides(List<Ride> rides) {
        this.rides = rides;
    }
}
