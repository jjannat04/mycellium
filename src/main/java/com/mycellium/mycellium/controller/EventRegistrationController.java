package com.mycellium.mycellium.controller;

import com.mycellium.mycellium.model.Event;
import com.mycellium.mycellium.model.Registration;
import com.mycellium.mycellium.model.RegistrationMember;
import com.mycellium.mycellium.model.User;
import com.mycellium.mycellium.repository.EventRepository;
import com.mycellium.mycellium.repository.RegistrationMemberRepository;
import com.mycellium.mycellium.repository.RegistrationRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
public class EventRegistrationController {

    private static final String STUDENT_ROLE = "STUDENT";
    private static final String REGISTERED_STATUS = "REGISTERED";

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private RegistrationMemberRepository registrationMemberRepository;

    @GetMapping("/events/{id}/register")
    public String showRegistrationForm(@PathVariable("id") Long eventId,
                                       HttpSession session,
                                       RedirectAttributes redirectAttributes,
                                       Model model) {
        User loggedInUser = getStudent(session);
        if (loggedInUser == null) {
            return "redirect:/auth/login";
        }

        Event event = eventRepository.findById(eventId).orElse(null);
        if (event == null) {
            return "redirect:/";
        }

        String unavailableReason = registrationUnavailableReason(event, loggedInUser.getEmail());
        if (unavailableReason != null) {
            redirectAttributes.addAttribute("error", unavailableReason);
            return "redirect:/events/" + eventId;
        }

        model.addAttribute("event", event);
        model.addAttribute("student", loggedInUser);
        model.addAttribute("defaultTeamSize", event.getMinTeamSize() == null ? 1 : event.getMinTeamSize());
        model.addAttribute("paidEvent", isPaid(event));
        return "event_register";
    }

    @PostMapping("/events/{id}/register")
    public String registerForEvent(@PathVariable("id") Long eventId,
                                   @RequestParam(value = "teamName", required = false) String teamName,
                                   @RequestParam(value = "teamSize", required = false) Integer teamSize,
                                   @RequestParam(value = "teamLeaderName", required = false) String teamLeaderName,
                                   @RequestParam(value = "teamLeaderEmail", required = false) String teamLeaderEmail,
                                   @RequestParam(value = "transactionId", required = false) String transactionId,
                                   @RequestParam(value = "memberNames", required = false) List<String> memberNames,
                                   @RequestParam(value = "memberEmails", required = false) List<String> memberEmails,
                                   HttpSession session,
                                   RedirectAttributes redirectAttributes) {
        User loggedInUser = getStudent(session);
        if (loggedInUser == null) {
            return "redirect:/auth/login";
        }

        Event event = eventRepository.findById(eventId).orElse(null);
        if (event == null) {
            redirectAttributes.addAttribute("error", "Event not found.");
            return "redirect:/";
        }

        String unavailableReason = registrationUnavailableReason(event, loggedInUser.getEmail());
        if (unavailableReason != null) {
            redirectAttributes.addAttribute("error", unavailableReason);
            return "redirect:/events/" + eventId;
        }

        Integer normalizedTeamSize = teamSize == null ? 1 : teamSize;
        String teamSizeError = validateTeamSize(event, normalizedTeamSize);
        if (teamSizeError != null) {
            redirectAttributes.addAttribute("error", teamSizeError);
            return "redirect:/events/" + eventId + "/register";
        }

        if (isPaid(event) && isBlank(transactionId)) {
            redirectAttributes.addAttribute("error", "Transaction ID is required for paid registration.");
            return "redirect:/events/" + eventId + "/register";
        }

        Registration registration = new Registration(eventId, loggedInUser.getEmail());
        registration.setSegmentId(null);
        registration.setTeamName(blankToNull(teamName));
        registration.setTeamSize(normalizedTeamSize);
        registration.setTeamLeaderName(blankToDefault(teamLeaderName, loggedInUser.getName()));
        registration.setTeamLeaderEmail(blankToDefault(teamLeaderEmail, loggedInUser.getEmail()));
        registration.setTransactionId(blankToNull(transactionId));
        registration.setRegistrationScope("EVENT");
        registration.setStatus(REGISTERED_STATUS);

        try {
            Registration savedRegistration = registrationRepository.save(registration);
            saveMembers(savedRegistration.getId(), memberNames, memberEmails);
        } catch (DataIntegrityViolationException ex) {
            redirectAttributes.addAttribute("error", "You are already registered.");
            return "redirect:/events/" + eventId;
        }

        redirectAttributes.addAttribute("message", "Registration confirmed.");
        return "redirect:/student/dashboard";
    }

    private User getStudent(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object user = session.getAttribute("loggedInUser");
        if (!(user instanceof User loggedInUser) || !STUDENT_ROLE.equals(loggedInUser.getRole())) {
            return null;
        }
        return loggedInUser;
    }

    private String registrationUnavailableReason(Event event, String studentEmail) {
        if (!"PUBLISHED".equals(event.getStatus())) {
            return "Registration is not open for this event.";
        }
        if (event.getRegistrationDeadline() != null && LocalDateTime.now().isAfter(event.getRegistrationDeadline())) {
            return "Registration deadline has passed.";
        }
        if (registrationRepository.existsActiveRegistration(event.getId(), null, studentEmail)) {
            return "You are already registered.";
        }
        if (event.getCapacity() != null && registrationRepository.countByEventIdAndStatus(event.getId(), REGISTERED_STATUS) >= event.getCapacity()) {
            return "This event is already full.";
        }
        return null;
    }

    private String validateTeamSize(Event event, Integer requestedTeamSize) {
        if (requestedTeamSize == null || requestedTeamSize < 1) {
            return "Team size must be at least 1.";
        }
        Integer minTeamSize = event.getMinTeamSize() == null ? 1 : event.getMinTeamSize();
        Integer maxTeamSize = event.getMaxTeamSize();
        if (requestedTeamSize < minTeamSize) {
            return "Team size must be at least " + minTeamSize + ".";
        }
        if (maxTeamSize != null && requestedTeamSize > maxTeamSize) {
            return "Team size cannot exceed " + maxTeamSize + ".";
        }
        return null;
    }

    private boolean isPaid(Event event) {
        return event.getFee() != null && event.getFee().compareTo(BigDecimal.ZERO) > 0;
    }

    private void saveMembers(Long registrationId, List<String> memberNames, List<String> memberEmails) {
        int totalRows = Math.max(memberNames == null ? 0 : memberNames.size(), memberEmails == null ? 0 : memberEmails.size());
        for (int i = 0; i < totalRows; i++) {
            String memberName = valueAt(memberNames, i);
            String memberEmail = valueAt(memberEmails, i);
            if (!isBlank(memberName) || !isBlank(memberEmail)) {
                registrationMemberRepository.save(new RegistrationMember(registrationId, blankToNull(memberName), blankToNull(memberEmail)));
            }
        }
    }

    private String valueAt(List<String> values, int index) {
        return values != null && index < values.size() ? values.get(index) : null;
    }

    private String blankToDefault(String value, String defaultValue) {
        return isBlank(value) ? defaultValue : value.trim();
    }

    private String blankToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
