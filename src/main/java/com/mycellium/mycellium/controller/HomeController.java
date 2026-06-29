package com.mycellium.mycellium.controller;

import com.mycellium.mycellium.model.Event;
import com.mycellium.mycellium.model.EventTimeline;
import com.mycellium.mycellium.model.User;
import com.mycellium.mycellium.repository.EventCategoryRepository;
import com.mycellium.mycellium.repository.EventRepository;
import com.mycellium.mycellium.repository.EventTimelineRepository;
import com.mycellium.mycellium.repository.RegistrationRepository;
import com.mycellium.mycellium.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventCategoryRepository eventCategoryRepository;

    @Autowired
    private EventTimelineRepository eventTimelineRepository;

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/")
    public String showPublicFeed(@RequestParam(value = "sort", defaultValue = "date") String sort,
                                 @RequestParam(value = "category", required = false) String category,
                                 @RequestParam(value = "page", defaultValue = "0") int page,
                                 Model model) {
        int safePage = Math.max(page, 0);
        Pageable pageable = PageRequest.of(safePage, 9, sortFor(sort));
        String normalizedCategory = category == null || category.isBlank() || "All".equalsIgnoreCase(category) ? null : category;

        Page<Event> eventPage = "popularity".equals(sort)
                ? eventRepository.findAllOrderByPopularity(normalizedCategory, PageRequest.of(safePage, 9))
                : normalizedCategory == null
                ? eventRepository.findByStatus("PUBLISHED", pageable)
                : eventRepository.findByStatusAndCategory("PUBLISHED", normalizedCategory, pageable);

        model.addAttribute("publicEvents", eventPage.getContent());
        model.addAttribute("eventPage", eventPage);
        model.addAttribute("sort", sort);
        model.addAttribute("category", normalizedCategory == null ? "All" : normalizedCategory);
        model.addAttribute("categories", eventCategoryRepository.findAllByOrderByNameAsc());
        model.addAttribute("studentCount", userRepository.countByRole("STUDENT"));

        return "index";
    }

    @GetMapping("/events/{id}")
    public String showEventDetails(@PathVariable("id") Long id,
                                   @RequestParam(value = "message", required = false) String message,
                                   @RequestParam(value = "error", required = false) String error,
                                   HttpSession session,
                                   Model model) {
        Event event = eventRepository.findById(id).orElse(null);
        if (event == null) {
            return "redirect:/";
        }

        User loggedInUser = session == null ? null : (User) session.getAttribute("loggedInUser");
        boolean organizerOwner = loggedInUser != null
                && "ORGANIZER".equals(loggedInUser.getRole())
                && loggedInUser.getEmail() != null
                && loggedInUser.getEmail().equals(event.getOrganizerEmail());
        if (!"PUBLISHED".equals(event.getStatus()) && !organizerOwner) {
            return "redirect:/";
        }

        List<EventTimeline> timelineItems = eventTimelineRepository.findByEventIdOrderByDisplayOrderAscTimelineDateAsc(event.getId());
        boolean studentLoggedIn = loggedInUser != null && "STUDENT".equals(loggedInUser.getRole());
        long activeRegistrations = registrationRepository.countByEventIdAndStatus(event.getId(), "REGISTERED");
        boolean eventFull = event.getCapacity() != null && activeRegistrations >= event.getCapacity();

        model.addAttribute("event", event);
        model.addAttribute("timelineItems", timelineItems);
        model.addAttribute("organizerOwner", organizerOwner);
        model.addAttribute("activeRegistrations", activeRegistrations);
        model.addAttribute("remainingSeats", event.getCapacity() == null ? null : Math.max(event.getCapacity() - activeRegistrations, 0));
        model.addAttribute("eventFull", eventFull);
        model.addAttribute("eventRegistrationClosed", !"PUBLISHED".equals(event.getStatus()) || isPastDeadline(event.getRegistrationDeadline()));
        model.addAttribute("message", message);
        model.addAttribute("error", error);
        User organizer = userRepository.findByEmail(event.getOrganizerEmail());
        model.addAttribute("organizerName", organizer != null && organizer.getName() != null && !organizer.getName().isBlank()
                ? organizer.getName()
                : event.getOrganizerEmail());
        if (studentLoggedIn) {
            model.addAttribute("registeredForEvent", registrationRepository.existsActiveRegistration(event.getId(), null, loggedInUser.getEmail()));
        } else {
            model.addAttribute("registeredForEvent", false);
        }
        return "event_details";
    }

    private Sort sortFor(String sort) {
        return switch (sort) {
            case "university" -> Sort.by(Sort.Order.asc("university"), Sort.Order.asc("title"));
            case "name" -> Sort.by(Sort.Order.asc("title"));
            case "type" -> Sort.by(Sort.Order.asc("eventType"), Sort.Order.asc("category"));
            case "date" -> Sort.by(Sort.Order.asc("startDate"), Sort.Order.asc("date"));
            default -> Sort.by(Sort.Order.asc("startDate"), Sort.Order.asc("date"));
        };
    }

    private boolean isPastDeadline(LocalDateTime deadline) {
        return deadline != null && LocalDateTime.now().isAfter(deadline);
    }
}
