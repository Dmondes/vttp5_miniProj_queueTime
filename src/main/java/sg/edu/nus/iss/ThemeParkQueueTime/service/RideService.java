package sg.edu.nus.iss.ThemeParkQueueTime.service;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;
import java.io.StringReader;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import sg.edu.nus.iss.ThemeParkQueueTime.model.LandRide;
import sg.edu.nus.iss.ThemeParkQueueTime.model.Ride;

@Service
public class RideService {

    RestTemplate restTemplate = new RestTemplate();
    private static final String BASE_URL = "https://queue-times.com/parks/";

    public List<Ride> getQueueTimes(String parkId) {
        // First get the park ID from Redis
        if (parkId == null) {
            throw new RuntimeException("Park not found");
        }

        String url = BASE_URL + parkId + "/queue_times.json";
        try {
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                String.class
            );

            if (
                response.getStatusCode() == HttpStatus.OK &&
                response.getBody() != null
            ) {
                return parseQueueTimesResponse(response.getBody());
            } else {
                throw new RuntimeException(
                    "Failed to fetch queue times. Status code: " +
                    response.getStatusCode()
                );
            }
        } catch (Exception e) {
            System.err.println("Error fetching queue times: " + e.getMessage());
            throw new RuntimeException("Unable to fetch queue times", e);
        }
    }

    private List<Ride> parseQueueTimesResponse(String jsonResponse) {
        List<Ride> allRides = new ArrayList<>();
        JsonReader jsonReader = Json.createReader(
            new StringReader(jsonResponse)
        );
        JsonObject ridesObj = jsonReader.readObject();

        // Parse lands array
        JsonArray landsArray = ridesObj.getJsonArray("lands");
        if (landsArray != null && !landsArray.isEmpty()) {
            for (JsonValue landValue : landsArray) {
                JsonObject landObject = landValue.asJsonObject();
                LandRide land = new LandRide();
                land.setId(landObject.getInt("id"));
                land.setName(landObject.getString("name"));

                List<Ride> landRides = new ArrayList<>();
                JsonArray ridesArray = landObject.getJsonArray("rides");

                for (JsonValue rideValue : ridesArray) {
                    JsonObject rideObject = rideValue.asJsonObject();
                    Ride ride = createRide(rideObject);
                    landRides.add(ride);
                    allRides.add(ride);
                }

                land.setRides(landRides);
            }
        }

        // Parse rides array from ridesObj
        JsonArray rootRidesArray = ridesObj.getJsonArray("rides");
        if (rootRidesArray != null && !rootRidesArray.isEmpty()) {
            for (JsonValue rideValue : rootRidesArray) {
                JsonObject rideObject = rideValue.asJsonObject();
                Ride ride = createRide(rideObject);
                allRides.add(ride);
            }
        }

        jsonReader.close();
        return allRides;
    }

    private Ride createRide(JsonObject rideObject) {
        Ride ride = new Ride();
        ride.setId(rideObject.getInt("id"));
        ride.setName(rideObject.getString("name"));
        ride.setWaitTime(rideObject.getInt("wait_time", 0));
        ride.setOpen(rideObject.getBoolean("is_open", false));

        if (rideObject.containsKey("last_updated")) {
            String lastUpdatedStr = rideObject.getString("last_updated");
            //Parse to localdatetime format
            ride.setLastUpdated(
                Instant.parse(lastUpdatedStr)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime()
            );
        }

        return ride;
    }

    public List<Ride> sortByWaitTime(List<Ride> rides) {
        return rides
            .stream()
            .sorted((r1, r2) -> {
                // First compare by open status (open rides first)
                if (r1.isOpen() && !r2.isOpen()) return -1;
                if (!r1.isOpen() && r2.isOpen()) return 1;

                // If both are closed, keep original order
                if (!r1.isOpen() && !r2.isOpen()) return 0;

                // If both are open, sort by wait time
                return Integer.compare(r1.getWaitTime(), r2.getWaitTime());
            })
            .collect(Collectors.toList());
    }
}
