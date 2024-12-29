package sg.edu.nus.iss.ThemeParkQueueTime.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sg.edu.nus.iss.ThemeParkQueueTime.model.User;
import sg.edu.nus.iss.ThemeParkQueueTime.service.UserService;

@Controller
@RequestMapping("/")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String showLoginPage() {
        return "index";
    }

    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/login")
    public String login(
        @RequestParam String username,
        @RequestParam String password,
        HttpSession session,
        Model model
    ) {
        try {
            User user = userService.authenticate(username, password);
            session.setAttribute("user", user);
            return "redirect:/dashboard"; // redirect as a new GET request
        } catch (Exception e) {
            model.addAttribute("error", "Invalid credentials");
            return "index";
        }
    }

    @PostMapping("/register")
    public String register(
        @Valid @ModelAttribute("user") User user,
        BindingResult result,
        Model model
    ) {
        if (result.hasErrors()) {
            return "register";
        }
        try {
            userService.saveUser(user);
            return "redirect:/?registered=true";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    @PostMapping("/saveRide")
    public String saveRide(
        @RequestParam String parkId,
        @RequestParam String rideId,
        HttpSession session,
        RedirectAttributes redirectAttributes
    ) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:";
        }
        userService.addFavoriteRide(user.getUsername(), parkId, rideId);
        redirectAttributes.addFlashAttribute(
            "message",
            "Ride saved successfully"
        );
        return "redirect:/park/" + parkId + "/queue-times";
    }

    @PostMapping("/removeRide")
    public String removeRide(
        @RequestParam String parkId,
        @RequestParam String rideId,
        HttpSession session
    ) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:";
        }
        userService.removeFavoriteRide(user.getUsername(), parkId, rideId);
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:";
        }
        model.addAttribute("user", user);
        model.addAttribute(
            "savedRides",
            userService.getSavedRides(user.getUsername())
        );
        return "dashboard";
    }

    @PostMapping("/dashboard/refresh")
    public String refreshDashboard(
        HttpSession session,
        RedirectAttributes redirectAttributes
    ) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/";
        }

        try {
            // Get fresh data for all saved rides
            userService.refreshSavedRides(user.getUsername());
            redirectAttributes.addFlashAttribute(
                "message",
                "Wait times refreshed successfully"
            );
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute(
                "error",
                "Failed to refresh wait times: " + e.getMessage()
            );
        }

        return "redirect:/dashboard";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:";
    }
}
