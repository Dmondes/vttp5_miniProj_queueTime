package sg.edu.nus.iss.ThemeParkQueueTime.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sg.edu.nus.iss.ThemeParkQueueTime.model.Ride;
import sg.edu.nus.iss.ThemeParkQueueTime.model.User;
import sg.edu.nus.iss.ThemeParkQueueTime.repo.KeyRepo;
import sg.edu.nus.iss.ThemeParkQueueTime.repo.MapRepo;

@Service
public class UserService {

    @Autowired
    private KeyRepo keyRepo;

    @Autowired
    private MapRepo mapRepo;

    @Autowired
    private RideService rideService;

    public void save(User user) {
        String key = ":" + user.getUsername();
        mapRepo.setMapAll(key, convertUserToMap(user));
    }

    public User findByUsername(String username) {
        String key = ":" + username;
        Map<Object, Object> userData = mapRepo.allEntries(key);
        return convertMapToUser(userData);
    }

    public boolean exists(String username) {
        return keyRepo.hasKey(":" + username);
    }

    private Map<String, String> convertUserToMap(User user) {
        Map<String, String> map = new HashMap<>();
        map.put("username", user.getUsername());
        map.put("email", user.getEmail());
        map.put("password", user.getPassword());
        map.put("savedRides", String.join(",", user.getSavedRides()));
        return map;
    }

    private User convertMapToUser(Map<Object, Object> map) {
        if (map.isEmpty()) {
            return null;
        }
        User user = new User();
        user.setUsername((String) map.get("username"));
        user.setEmail((String) map.get("email"));
        user.setPassword((String) map.get("password"));

        String savedRidesStr = (String) map.get("savedRides");
        if (savedRidesStr != null && !savedRidesStr.isEmpty()) {
            user.getSavedRides().addAll(List.of(savedRidesStr.split(",")));
        }
        return user;
    }

    public void saveUser(User user) {
        if (exists(user.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        save(user);
    }

    public User getUser(String username) {
        return findByUsername(username);
    }

    public User authenticate(String username, String password) {
        User user = findByUsername(username);
        if (user != null && password.equals(user.getPassword())) {
            return user;
        }
        throw new RuntimeException("Invalid credentials");
    }

    public void addFavoriteRide(String username, String parkId, String rideId) {
        User user = getUser(username);
        if (user != null) {
            user.getSavedRides().add(parkId + ":" + rideId); // Store both park and ride ID
            save(user);
        }
    }

    public void removeFavoriteRide(
        String username,
        String parkId,
        String rideId
    ) {
        User user = getUser(username);
        if (user != null) {
            user.getSavedRides().remove(parkId + ":" + rideId);
            save(user);
        }
    }

    public void refreshSavedRides(String username) {
        User user = getUser(username);
        if (user != null && !user.getSavedRides().isEmpty()) {
            // Create a new set to store updated ride IDs
            Set<String> updatedRides = new HashSet<>();

            // Process each saved ride
            for (String combinedId : user.getSavedRides()) {
                String[] parts = combinedId.split(":");
                if (parts.length == 2) {
                    String parkId = parts[0];
                    String rideId = parts[1];

                    // Get fresh queue times from API
                    List<Ride> freshParkRides = rideService.getQueueTimes(
                        parkId
                    );

                    // Check if ride still exists
                    boolean rideExists = freshParkRides
                        .stream()
                        .anyMatch(ride ->
                            String.valueOf(ride.getId()).equals(rideId)
                        );

                    if (rideExists) {
                        updatedRides.add(combinedId);
                    }
                }
            }

            // Update user's saved rides
            user.setSavedRides(updatedRides);
            save(user);
        }
    }

    public Map<String, List<Ride>> getSavedRides(String username) {
        User user = getUser(username);
        if (user != null && !user.getSavedRides().isEmpty()) {
            Map<String, List<Ride>> ridesWithParkId = new HashMap<>();

            for (String combinedId : user.getSavedRides()) {
                String[] parts = combinedId.split(":");
                if (parts.length == 2) {
                    String parkId = parts[0];
                    String rideId = parts[1];

                    // Always get fresh data from API
                    List<Ride> freshParkRides = rideService.getQueueTimes(
                        parkId
                    );
                    freshParkRides
                        .stream()
                        .filter(ride ->
                            String.valueOf(ride.getId()).equals(rideId)
                        )
                        .findFirst()
                        .ifPresent(ride -> {
                            if (!ridesWithParkId.containsKey(parkId)) {
                                ridesWithParkId.put(parkId, new ArrayList<>());
                            }
                            ridesWithParkId.get(parkId).add(ride);
                        });
                }
            }
            return ridesWithParkId;
        }
        return new HashMap<>();
    }
}
