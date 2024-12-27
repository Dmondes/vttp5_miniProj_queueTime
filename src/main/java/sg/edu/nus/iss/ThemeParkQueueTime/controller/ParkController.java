package sg.edu.nus.iss.ThemeParkQueueTime.controller;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import sg.edu.nus.iss.ThemeParkQueueTime.model.Park;
import sg.edu.nus.iss.ThemeParkQueueTime.model.Ride;
import sg.edu.nus.iss.ThemeParkQueueTime.service.RideService;
import sg.edu.nus.iss.ThemeParkQueueTime.service.ThemeParkService;

@Controller
@RequestMapping("/")
public class ParkController {

    @Autowired
    private ThemeParkService themeParkService;

    @Autowired
    private RideService rideService;

    @GetMapping("/countries")
    public String listCountries(Model model) {
        Set<String> countries = themeParkService.getCountryKeys(".*"); //start with prefix
        Set<String> removedDot = countries
            .stream()
            .map(country -> country.replaceFirst("^\\.", ""))
            .collect(Collectors.toSet());
        model.addAttribute("countries", removedDot);
        return "countries";
    }

    @GetMapping("/country/{country}")
    public String showCountryParks(@PathVariable String country, Model model) {
        List<Park> parks = themeParkService.getParksFromCountry(country);
        parks.forEach(park ->
            park.setName(park.getName().replaceAll("[\"']", ""))
        );
        model.addAttribute("country", country);
        model.addAttribute("parks", parks);
        return "countryParks";
    }

    @GetMapping("/park/{parkId}/queue-times")
    public String showQueueTimes(
        @PathVariable String parkId,
        @RequestParam(required = false) String sort,
        @RequestParam(required = false) String country,
        Model model
    ) {
        List<Ride> queueTimes = rideService.getQueueTimes(parkId);

        if ("waittime".equals(sort)) {
            queueTimes = rideService.sortByWaitTime(queueTimes);
        }

        if (country == null) {
            Park park = themeParkService.getParkById(parkId);
            country = park.getCountry();
        }

        model.addAttribute("queueTimes", queueTimes);
        model.addAttribute("parkId", parkId);
        model.addAttribute("country", country);
        return "queueTimes";
    }
}
