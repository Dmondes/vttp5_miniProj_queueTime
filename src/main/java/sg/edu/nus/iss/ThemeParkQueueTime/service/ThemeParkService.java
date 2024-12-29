package sg.edu.nus.iss.ThemeParkQueueTime.service;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import sg.edu.nus.iss.ThemeParkQueueTime.model.Park;
import sg.edu.nus.iss.ThemeParkQueueTime.model.ParkList;
import sg.edu.nus.iss.ThemeParkQueueTime.repo.KeyRepo;
import sg.edu.nus.iss.ThemeParkQueueTime.repo.ListRepo;

@Service
public class ThemeParkService {

    RestTemplate restTemplate = new RestTemplate();
    private static final String BASE_URL = "https://queue-times.com";

    @Autowired
    private KeyRepo keyRepo;

    @Autowired
    private ListRepo listRepo;

    public Set<String> getAllParks() {
        String url = BASE_URL + "/parks.json";
        Set<String> processedCountries = new HashSet<>();
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
                List<ParkList> parkCompanies = parseParksResponse(
                    response.getBody()
                );
                for (ParkList company : parkCompanies) {
                    processParksFromCompany(company, processedCountries);
                }
                System.out.println("processedCountries");
                return processedCountries;
            } else {
                throw new RuntimeException(
                    "Failed to fetch parks data. Status code: " +
                    response.getStatusCode()
                );
            }
        } catch (Exception e) {
            System.err.println("Error fetching parks data: " + e.getMessage());
            throw new RuntimeException("Unable to fetch parks data", e);
        }
    }

    private void processParksFromCompany(
        ParkList company,
        Set<String> processedCountries
    ) {
        // Group parks by country, filtering out parks without a country
        Map<String, List<Park>> parksByCountry = company
            .getParks()
            .stream()
            .filter(
                park ->
                    park.getCountry() != null &&
                    !park.getCountry().trim().isEmpty()
            )
            .collect(Collectors.groupingBy(Park::getCountry));
        System.out.println("Group parks");

        // Save parks for each country
        parksByCountry.forEach((country, parks) -> {
            // Create Redis key for this country
            String redisKey = "." + country;

            // Remove duplicates based on park ID
            List<Park> uniqueParks = parks
                .stream()
                .collect(
                    Collectors.toMap(
                        Park::getId,
                        park -> park,
                        (existing, replacement) -> existing //keep existing if dup is found
                    )
                )
                .values()
                .stream()
                .collect(Collectors.toList());
            // Save unique parks to Redis list
            String parkStrings = uniqueParks
                .stream()
                .map(Park::toString)
                .collect(Collectors.joining(","));
            // System.out.println("Unique park saved"); // check park
            listRepo.addToBackListAll(redisKey, parkStrings);
            // System.out.println("check add"); //check add
            // Track processed countries
            processedCountries.add(country);
        });
        // System.out.println("done");
    }

    public List<ParkList> parseParksResponse(String jsonResponse) {
        // Create a JSON reader from the raw string
        JsonReader jsonReader = Json.createReader(
            new StringReader(jsonResponse)
        );
        JsonArray parkCompaniesArray = jsonReader.readArray();

        // List to store parsed park companies
        List<ParkList> parsedCompanies = new ArrayList<>();

        // Iterate through each JSON object in the array
        for (JsonValue jsonValue : parkCompaniesArray) {
            JsonObject companyObject = jsonValue.asJsonObject();

            ParkList parkCompany = new ParkList();

            // Parse basic company details
            parkCompany.setId(companyObject.getInt("id"));
            parkCompany.setName(companyObject.getString("name"));

            // Parse parks array
            JsonArray parksArray = companyObject.getJsonArray("parks");
            List<Park> parks = new ArrayList<>();

            // Iterate through parks
            for (JsonValue parkValue : parksArray) {
                JsonObject parkObject = parkValue.asJsonObject();

                // Create Park instance
                Park park = new Park();
                park.setId(parkObject.getInt("id"));
                park.setName(parkObject.getString("name"));
                park.setCountry(parkObject.getString("country"));
                park.setContinent(parkObject.getString("continent"));
                park.setLatitude(parkObject.getString("latitude"));
                park.setLongitude(parkObject.getString("longitude"));
                park.setTimezone(parkObject.getString("timezone"));
                parks.add(park);
            }
            parkCompany.setParks(parks); // Set parks for the company
            parsedCompanies.add(parkCompany); // Add to list of companies
        }
        jsonReader.close();

        return parsedCompanies;
    }

    public Set<String> getCountryKeys(String pattern) {
        return keyRepo.keys(pattern);
    }

    public List<Park> getParksFromCountry(String country) {
        String restoredKey = "." + country;
        String countryParks = listRepo.getList(restoredKey).toString();
        List<Park> parks = new ArrayList<>();
        for (String parkString : countryParks.split(
            "(?<=\\}),\\s*(?=Park\\{)"
        )) {
            Park newPark = new Park();
            int nameStart = parkString.indexOf("name='") + 5;
            int nameEnd = parkString.indexOf(",", nameStart);
            if (nameStart >= 6 && nameEnd > nameStart) {
                String cutString = parkString.substring(nameStart, nameEnd);
                newPark.setName(cutString);
            }
            int idStart = parkString.indexOf("id=") + 3;
            int idEnd = parkString.indexOf(",", idStart);
            if (idStart >= 3 && idEnd > idStart) {
                String idString = parkString.substring(idStart, idEnd);
                newPark.setId(Integer.valueOf(idString));
            }
            parks.add(newPark);
        }
        return parks;
    }

    public Park getParkById(String parkId) {
        try {
            int id = Integer.parseInt(parkId);
            Set<String> countries = getCountryKeys(".*");

            for (String country : countries) {
                List<Park> parks = getParksFromCountry(
                    country.replaceFirst("^\\.", "")
                );
                Optional<Park> foundPark = parks
                    .stream()
                    .filter(park -> park.getId() == id)
                    .findFirst();

                if (foundPark.isPresent()) {
                    Park park = foundPark.get();
                    park.setCountry(country.replaceFirst("^\\.", ""));
                    return park;
                }
            }

            System.out.println("Park not found with ID: " + parkId);
            return null;
        } catch (NumberFormatException e) {
            System.err.println("Invalid park ID format: " + parkId);
            return null;
        } catch (Exception e) {
            System.err.println(
                "Error finding park with ID " + parkId + ": " + e.getMessage()
            );
            return null;
        }
    }
}
