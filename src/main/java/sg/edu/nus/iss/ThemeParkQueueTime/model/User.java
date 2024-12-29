package sg.edu.nus.iss.ThemeParkQueueTime.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

public class User {

    @NotBlank(message = "Please enter username")
    @Size(min = 3, max = 20, message = "Username between 3 to 20 characters")
    private String username;

    @NotBlank(message = "Please enter email")
    @Email(message = "Invalid email format")
    @Pattern(
        regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
        message = "Invalid email format"
    )
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    private Set<String> savedRides = new HashSet<>();

    public User(
        @NotBlank(message = "Please enter username") @Size(
            min = 3,
            max = 20,
            message = "Username between 3 to 20 characters"
        ) String username,
        @NotBlank(message = "Please enter email") @Email(
            message = "Invalid email format"
        ) @Pattern(
            regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
            message = "Invalid email format"
        ) String email,
        @NotBlank(message = "Password is required") @Size(
            min = 6,
            message = "Password must be at least 6 characters"
        ) String password,
        Set<String> savedRides
    ) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.savedRides = savedRides;
    }

    public User() {}

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<String> getSavedRides() {
        return savedRides;
    }

    public void setSavedRides(Set<String> savedRides) {
        this.savedRides = savedRides;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
