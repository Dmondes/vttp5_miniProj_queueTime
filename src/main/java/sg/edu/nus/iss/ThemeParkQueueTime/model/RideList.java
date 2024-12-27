package sg.edu.nus.iss.ThemeParkQueueTime.model;

import java.util.List;
public class RideList {

    private List<LandRide> lands;
    private List<Ride> rides;

    // Getters and Setters
    public List<LandRide> getLands() {
        return lands;
    }

    public void setLands(List<LandRide> lands) {
        this.lands = lands;
    }

    public List<Ride> getRides() {
        return rides;
    }

    public void setRides(List<Ride> rides) {
        this.rides = rides;
    }

}
