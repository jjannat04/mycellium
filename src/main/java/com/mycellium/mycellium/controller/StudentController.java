package com.mycellium.mycellium.controller;

import com.mycellium.mycellium.model.Event;
import com.mycellium.mycellium.model.EventSegment;
import com.mycellium.mycellium.model.Registration;
import com.mycellium.mycellium.model.User;
import com.mycellium.mycellium.repository.EventRepository;
import com.mycellium.mycellium.repository.EventSegmentRepository;
import com.mycellium.mycellium.repository.RegistrationRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/student")
public class StudentController {

    private static final String STUDENT_ROLE = "STUDENT";
    private static final String REGISTERED_STATUS = "REGISTERED";
    private static final String CANCELLED_STATUS = "CANCELLED";

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventSegmentRepository eventSegmentRepository;

    @GetMapping("/dashboard")
    public String showStudentDashboard(HttpSession session,
                                       @RequestParam(value = "message", required = false) String message,
                                       @RequestParam(value = "error", required = false) String error,
                                       Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            return "redirect:/auth/login";
        }
        if (!STUDENT_ROLE.equals(loggedInUser.getRole())) {
            return "redirect:/";
        }

        List<Registration> registrations = registrationRepository.findByStudentEmailAndStatus(loggedInUser.getEmail(), REGISTERED_STATUS);
        Map<Long, Event> eventMap = new HashMap<>();
        Map<Long, EventSegment> segmentMap = new HashMap<>();

        for (Registration reg : registrations) {
            eventRepository.findById(reg.getEventId()).ifPresent(event -> eventMap.put(reg.getId(), event));
            if (reg.getSegmentId() != null) {
                eventSegmentRepository.findById(reg.getSegmentId()).ifPresent(segment -> segmentMap.put(reg.getId(), segment));
            }
        }

        model.addAttribute("student", loggedInUser);
        model.addAttribute("registrations", registrations);
        model.addAttribute("eventMap", eventMap);
        model.addAttribute("segmentMap", segmentMap);
        model.addAttribute("registeredEvents", registrations);
        model.addAttribute("rsvpCount", registrationRepository.countByStudentEmailAndStatus(loggedInUser.getEmail(), REGISTERED_STATUS));
        model.addAttribute("message", message);
        model.addAttribute("error", error);

        return "student_dashboard";
    }

    @PostMapping("/events/rsvp/{id}")
    public String rsvpToEvent(@PathVariable("id") Long eventId,
                              @RequestParam(value = "teamName", required = false) String teamName,
                              @RequestParam(value = "teamSize", required = false) Integer teamSize,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/auth/login";
        }
        if (!STUDENT_ROLE.equals(loggedInUser.getRole())) {
            return "redirect:/";
        }
        return "redirect:/events/" + eventId + "/register";
    }

    @PostMapping("/events/{eventId}/segments/{segmentId}/register")
    public String registerForSegment(@PathVariable("eventId") Long eventId,
                                     @PathVariable("segmentId") Long segmentId,
                                     @RequestParam(value = "teamName", required = false) String teamName,
                                     @RequestParam(value = "teamSize", required = false) Integer teamSize,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes) {
        return registerForEvent(eventId, segmentId, teamName, teamSize, session, redirectAttributes);
    }

    @PostMapping("/registrations/{registrationId}/cancel")
    public String cancelRegistration(@PathVariable("registrationId") Long registrationId,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            return "redirect:/auth/login";
        }
        if (!STUDENT_ROLE.equals(loggedInUser.getRole())) {
            return "redirect:/";
        }

        Registration registration = registrationRepository.findById(registrationId).orElse(null);
        if (registration == null || !loggedInUser.getEmail().equals(registration.getStudentEmail())) {
            redirectAttributes.addAttribute("error", "Registration not found.");
            return "redirect:/student/dashboard";
        }

        registration.setStatus(CANCELLED_STATUS);
        registrationRepository.save(registration);
        redirectAttributes.addAttribute("message", "Registration cancelled.");
        return "redirect:/student/dashboard";
    }

    private String registerForEvent(Long eventId,
                                    Long segmentId,
                                    String teamName,
                                    Integer teamSize,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            return "redirect:/auth/login";
        }
        if (!STUDENT_ROLE.equals(loggedInUser.getRole())) {
            return "redirect:/";
        }

        Event event = eventRepository.findById(eventId).orElse(null);
        if (event == null) {
            redirectAttributes.addAttribute("error", "Event not found.");
            return "redirect:/";
        }
        if (!"PUBLISHED".equals(event.getStatus())) {
            redirectAttributes.addAttribute("error", "Registration is not open for this event.");
            return "redirect:/events/" + eventId;
        }

        EventSegment segment = null;
        if (segmentId != null) {
            segment = eventSegmentRepository.findById(segmentId).orElse(null);
            if (segment == null || !eventId.equals(segment.getEventId())) {
                redirectAttributes.addAttribute("error", "Segment not found.");
                return "redirect:/events/" + eventId;
            }
        }
        if (segment == null && !eventSegmentRepository.findByEventIdOrderByCreatedAtAsc(eventId).isEmpty()) {
            redirectAttributes.addAttribute("error", "Please choose a segment to register for this event.");
            return "redirect:/events/" + eventId;
        }

        if (isPastDeadline(event.getRegistrationDeadline()) || (segment != null && isPastDeadline(segment.getRegistrationDeadline()))) {
            redirectAttributes.addAttribute("error", "Registration deadline has passed.");
            return "redirect:/events/" + eventId;
        }

        if (registrationRepository.existsActiveRegistration(eventId, segmentId, loggedInUser.getEmail())) {
            redirectAttributes.addAttribute("error", "You are already registered.");
            return "redirect:/events/" + eventId;
        }

        if (segment != null && segment.getCapacity() != null) {
            long activeSegmentRegistrations = registrationRepository.countBySegmentIdAndStatus(segment.getId(), REGISTERED_STATUS);
            if (activeSegmentRegistrations >= segment.getCapacity()) {
                redirectAttributes.addAttribute("error", "This segment is already full.");
                return "redirect:/events/" + eventId;
            }
        }

        Integer normalizedTeamSize = teamSize == null ? 1 : teamSize;
        String teamSizeError = validateTeamSize(segment, normalizedTeamSize);
        if (teamSizeError != null) {
            redirectAttributes.addAttribute("error", teamSizeError);
            return "redirect:/events/" + eventId;
        }

        Registration registration = new Registration(eventId, loggedInUser.getEmail());
        registration.setSegmentId(segmentId);
        registration.setTeamName(blankToNull(teamName));
        registration.setTeamSize(normalizedTeamSize);
        registration.setRegistrationScope(registrationScope(segmentId));
        registration.setStatus(REGISTERED_STATUS);
        registrationRepository.save(registration);

        redirectAttributes.addAttribute("message", "Registration confirmed.");
        return "redirect:/student/dashboard";
    }

    private boolean isPastDeadline(LocalDateTime deadline) {
        return deadline != null && LocalDateTime.now().isAfter(deadline);
    }

    private String blankToNull(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    private String registrationScope(Long segmentId) {
        return segmentId == null ? "EVENT" : "SEGMENT:" + segmentId;
    }

    private String validateTeamSize(EventSegment segment, Integer requestedTeamSize) {
        if (requestedTeamSize == null || requestedTeamSize < 1) {
            return "Team size must be at least 1.";
        }
        if (segment == null) {
            return null;
        }

        Integer minTeamSize = segment.getMinTeamSize() == null ? 1 : segment.getMinTeamSize();
        Integer maxTeamSize = segment.getMaxTeamSize() == null ? segment.getTeamSize() : segment.getMaxTeamSize();
        if (requestedTeamSize < minTeamSize) {
            return "Team size must be at least " + minTeamSize + ".";
        }
        if (maxTeamSize != null && requestedTeamSize > maxTeamSize) {
            return "Team size cannot exceed " + maxTeamSize + ".";
        }
        return null;
    }
}
