package sg.edu.nus.iss.ThemeParkQueueTime.model;

public class Park {

    private int id;
    private String name;
    private String country;
    private String continent;
    private String latitude;
    private String longitude;
    private String timezone;

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

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getContinent() {
        return continent;
    }

    public void setContinent(String continent) {
        this.continent = continent;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    @Override
    public String toString() {
        return ("Park{" + "id=" + id +
                ", name='" + name + '\'' +
                ", country='" + country +'\'' +
                ", continent='" + continent +'\'' +
                ", latitude='" + latitude +'\'' +
                ", longitude='" + longitude +'\'' +
                ", timezone='" + timezone +'\'' +
                '}');
    }
}
