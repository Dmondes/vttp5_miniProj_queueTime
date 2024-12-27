package sg.edu.nus.iss.ThemeParkQueueTime.restcontroller;

import jakarta.validation.Valid;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sg.edu.nus.iss.ThemeParkQueueTime.model.User;
import sg.edu.nus.iss.ThemeParkQueueTime.service.ThemeParkService;
import sg.edu.nus.iss.ThemeParkQueueTime.service.UserService;

@RestController
@RequestMapping("/api")
public class ParkRestController {

    @Autowired
    private ThemeParkService themeParkService;

    @Autowired
    private UserService userService;

    @GetMapping("/parks")
    public ResponseEntity<Set<String>> getAllParks() {
        try {
            Set<String> parks = themeParkService.getAllParks();
            return ResponseEntity.ok(parks);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping(
        value = "/users",
        consumes = { "application/json", "application/x-www-form-urlencoded" }
    )
    public ResponseEntity<String> createUser(@Valid @RequestBody User user) {
        try {
            userService.saveUser(user);
            return ResponseEntity.ok("User created successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/users/{username}")
    public ResponseEntity<User> getUser(@PathVariable String username) {
        try {
            User user = userService.getUser(username);
            if (user != null) {
                return ResponseEntity.ok(user);
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/users/{username}/rides/save")
    public ResponseEntity<String> addFavoriteRide(
        @PathVariable String username,
        @RequestParam String parkId,
        @RequestParam String rideId
    ) {
        try {
            if (!userService.exists(username)) {
                return ResponseEntity.notFound().build();
            }
            userService.addFavoriteRide(username, parkId, rideId);
            return ResponseEntity.ok("Ride saved successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/users/{username}/rides/remove")
    public ResponseEntity<String> removeFavoriteRide(
        @PathVariable String username,
        @RequestParam String parkId,
        @RequestParam String rideId
    ) {
        try {
            if (!userService.exists(username)) {
                return ResponseEntity.notFound().build();
            }
            userService.removeFavoriteRide(username, parkId, rideId);
            return ResponseEntity.ok("Ride removed successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/users/{username}/rides")
    public ResponseEntity<Set<String>> getUserRides(
        @PathVariable String username
    ) {
        try {
            User user = userService.getUser(username);
            if (user != null) {
                return ResponseEntity.ok(user.getSavedRides());
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
