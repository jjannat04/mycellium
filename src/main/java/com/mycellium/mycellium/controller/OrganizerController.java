package com.mycellium.mycellium.controller;

import com.mycellium.mycellium.model.Event;
import com.mycellium.mycellium.model.EventCategory;
import com.mycellium.mycellium.model.EventSegment;
import com.mycellium.mycellium.model.EventTimeline;
import com.mycellium.mycellium.model.Notification;
import com.mycellium.mycellium.model.Registration;
import com.mycellium.mycellium.model.User;
import com.mycellium.mycellium.repository.EventCategoryRepository;
import com.mycellium.mycellium.repository.EventRepository;
import com.mycellium.mycellium.repository.EventSegmentRepository;
import com.mycellium.mycellium.repository.EventTimelineRepository;
import com.mycellium.mycellium.repository.NotificationRepository;
import com.mycellium.mycellium.repository.RegistrationRepository;
import com.mycellium.mycellium.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/organizer")
public class OrganizerController {

    private static final String ORGANIZER_ROLE = "ORGANIZER";

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventCategoryRepository eventCategoryRepository;

    @Autowired
    private EventSegmentRepository eventSegmentRepository;

    @Autowired
    private EventTimelineRepository eventTimelineRepository;

    @Autowired
    private RegistrationRepository registrationRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    // This dynamically finds your system user home directory and targets a folder named 'mycellium-uploads'
    private static final String UPLOAD_DIR = System.getProperty("user.home") + File.separator + "mycellium-uploads" + File.separator;

    @GetMapping("/dashboard")
    public String showDashboard(HttpSession session,
                                @RequestParam(value = "message", required = false) String message,
                                @RequestParam(value = "error", required = false) String error,
                                Model model) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/auth/login";
        }
        if (!ORGANIZER_ROLE.equals(loggedInUser.getRole())) {
            return "redirect:/";
        }
        if (loggedInUser.getName() == null || loggedInUser.getName().trim().isEmpty()) {
            loggedInUser.setName("Organizer");
        }

        List<Event> organizerEvents = eventRepository.findByOrganizerEmail(loggedInUser.getEmail());
        List<Long> eventIds = organizerEvents.stream().map(Event::getId).collect(Collectors.toList());
        List<Registration> registrations = eventIds.isEmpty()
                ? Collections.emptyList()
                : registrationRepository.findByEventIds(eventIds);
        Map<Long, Long> eventRegistrationCounts = new HashMap<>();
        Map<Long, Long> segmentRegistrationCounts = new HashMap<>();
        Map<Long, String> eventTitleMap = new HashMap<>();
        Map<Long, String> segmentNameMap = new HashMap<>();

        for (Event event : organizerEvents) {
            eventRegistrationCounts.put(event.getId(), registrationRepository.countByEventIdAndStatus(event.getId(), "REGISTERED"));
            eventTitleMap.put(event.getId(), event.getTitle());
            for (EventSegment segment : eventSegmentRepository.findByEventIdOrderByCreatedAtAsc(event.getId())) {
                segmentNameMap.put(segment.getId(), segment.getName());
                segmentRegistrationCounts.put(segment.getId(), registrationRepository.countBySegmentIdAndStatus(segment.getId(), "REGISTERED"));
            }
        }

        model.addAttribute("organizer", loggedInUser);
        model.addAttribute("events", organizerEvents);
        model.addAttribute("activeEventsCount", organizerEvents.size());
        model.addAttribute("totalRegistrations", registrations.stream().filter(reg -> "REGISTERED".equals(reg.getStatus())).count());
        model.addAttribute("registrations", registrations);
        model.addAttribute("eventRegistrationCounts", eventRegistrationCounts);
        model.addAttribute("segmentRegistrationCounts", segmentRegistrationCounts);
        model.addAttribute("eventTitleMap", eventTitleMap);
        model.addAttribute("segmentNameMap", segmentNameMap);
        model.addAttribute("message", message);
        model.addAttribute("error", error);
        model.addAttribute("categories", eventCategoryRepository.findAllByOrderByNameAsc());

        return "dashboard";
    }

    @PostMapping("/events/create")
    public String createEvent(@ModelAttribute Event event,
                              @RequestParam("eventImage") MultipartFile file,
                              @RequestParam(value = "newCategory", required = false) String newCategory,
                              HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");

        if (loggedInUser == null) {
            return "redirect:/auth/login";
        }
        if (!ORGANIZER_ROLE.equals(loggedInUser.getRole())) {
            return "redirect:/";
        }

        event.setOrganizerEmail(loggedInUser.getEmail());
        event.setCategory(resolveCategory(event.getCategory(), newCategory));

        // Check if the user selected a file from their PC
        if (!file.isEmpty()) {
            try {
                // Create the upload directory on your local machine if it doesn't exist
                File directory = new File(UPLOAD_DIR);
                if (!directory.exists()) {
                    directory.mkdirs();
                }

                // Create a unique filename to prevent overwriting
                String uniqueFilename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                Path path = Paths.get(UPLOAD_DIR + uniqueFilename);

                // Physically write the bits from your PC to your storage directory
                Files.write(path, file.getBytes());

                // Save the URL route that Thymeleaf will read
                event.setImageUrl("/uploads/" + uniqueFilename);
            } catch (IOException e) {
                e.printStackTrace();
                event.setImageUrl("https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=800");
            }
        } else {
            // If the user didn't pick an image, use a beautiful default stock banner
            event.setImageUrl("https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=800");
        }

        Event savedEvent = eventRepository.save(event);
        createEventNotifications(savedEvent);
        return "redirect:/organizer/dashboard";
    }

    @PostMapping("/events/{eventId}/edit")
    public String editEvent(@PathVariable("eventId") Long eventId,
                            @ModelAttribute Event eventUpdate,
                            @RequestParam(value = "eventImage", required = false) MultipartFile file,
                            @RequestParam(value = "newCategory", required = false) String newCategory,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        Event event = findOwnedEvent(eventId, session);
        if (event == null) {
            return redirectForUnauthorizedEventAccess(session, redirectAttributes);
        }

        if (isBlank(eventUpdate.getTitle()) || isBlank(eventUpdate.getDate()) || isBlank(eventUpdate.getLocation())) {
            redirectAttributes.addAttribute("error", "Title, date, and location are required.");
            return "redirect:/organizer/dashboard";
        }

        event.setTitle(eventUpdate.getTitle().trim());
        event.setDate(eventUpdate.getDate().trim());
        event.setCategory(resolveCategory(eventUpdate.getCategory(), newCategory));
        event.setLocation(eventUpdate.getLocation().trim());
        event.setDescription(eventUpdate.getDescription());
        event.setUniversity(eventUpdate.getUniversity());
        event.setEventType(eventUpdate.getEventType());
        event.setSeriesName(eventUpdate.getSeriesName());
        event.setFee(eventUpdate.getFee());
        event.setMinTeamSize(eventUpdate.getMinTeamSize());
        event.setMaxTeamSize(eventUpdate.getMaxTeamSize());
        event.setCapacity(eventUpdate.getCapacity());
        event.setStartDate(eventUpdate.getStartDate());
        event.setEndDate(eventUpdate.getEndDate());
        event.setRegistrationDeadline(eventUpdate.getRegistrationDeadline());
        event.setStatus(eventUpdate.getStatus());
        event.setAbout(eventUpdate.getAbout());

        if (file != null && !file.isEmpty()) {
            event.setImageUrl(saveEventImage(file));
        }

        eventRepository.save(event);
        redirectAttributes.addAttribute("message", "Event updated.");
        return "redirect:/organizer/dashboard";
    }

    @PostMapping("/events/{eventId}/delete")
    public String deleteEvent(@PathVariable("eventId") Long eventId,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        Event event = findOwnedEvent(eventId, session);
        if (event == null) {
            return redirectForUnauthorizedEventAccess(session, redirectAttributes);
        }

        registrationRepository.deleteByEventId(eventId);
        eventSegmentRepository.deleteByEventId(eventId);
        eventTimelineRepository.deleteByEventId(eventId);
        eventRepository.delete(event);
        redirectAttributes.addAttribute("message", "Event deleted.");
        return "redirect:/organizer/dashboard";
    }

    @PostMapping("/events/{eventId}/segments/create")
    public String createSegment(@PathVariable("eventId") Long eventId,
                                @ModelAttribute EventSegment segment,
                                HttpSession session) {
        Event event = findOwnedEvent(eventId, session);
        if (event == null) {
            return redirectForUnauthorizedSegmentAccess(session);
        }

        segment.setId(null);
        segment.setEventId(event.getId());
        eventSegmentRepository.save(segment);
        return "redirect:/events/" + eventId;
    }

    @PostMapping("/events/{eventId}/timeline/create")
    public String createTimelineItem(@PathVariable("eventId") Long eventId,
                                     @ModelAttribute EventTimeline timelineItem,
                                     HttpSession session) {
        Event event = findOwnedEvent(eventId, session);
        if (event == null) {
            return redirectForUnauthorizedSegmentAccess(session);
        }

        timelineItem.setId(null);
        timelineItem.setEventId(event.getId());
        eventTimelineRepository.save(timelineItem);
        return "redirect:/events/" + eventId;
    }

    @PostMapping("/events/{eventId}/timeline/{timelineId}/edit")
    public String editTimelineItem(@PathVariable("eventId") Long eventId,
                                   @PathVariable("timelineId") Long timelineId,
                                   @ModelAttribute EventTimeline timelineItem,
                                   HttpSession session) {
        Event event = findOwnedEvent(eventId, session);
        if (event == null) {
            return redirectForUnauthorizedSegmentAccess(session);
        }

        EventTimeline existingTimelineItem = eventTimelineRepository.findById(timelineId).orElse(null);
        if (existingTimelineItem == null || !event.getId().equals(existingTimelineItem.getEventId())) {
            return "redirect:/events/" + eventId;
        }

        existingTimelineItem.setTitle(timelineItem.getTitle());
        existingTimelineItem.setDescription(timelineItem.getDescription());
        existingTimelineItem.setTimelineDate(timelineItem.getTimelineDate());
        existingTimelineItem.setDisplayOrder(timelineItem.getDisplayOrder());
        eventTimelineRepository.save(existingTimelineItem);

        return "redirect:/events/" + eventId;
    }

    @PostMapping("/events/{eventId}/timeline/{timelineId}/delete")
    public String deleteTimelineItem(@PathVariable("eventId") Long eventId,
                                     @PathVariable("timelineId") Long timelineId,
                                     HttpSession session) {
        Event event = findOwnedEvent(eventId, session);
        if (event == null) {
            return redirectForUnauthorizedSegmentAccess(session);
        }

        EventTimeline timelineItem = eventTimelineRepository.findById(timelineId).orElse(null);
        if (timelineItem != null && event.getId().equals(timelineItem.getEventId())) {
            eventTimelineRepository.delete(timelineItem);
        }

        return "redirect:/events/" + eventId;
    }

    @PostMapping("/events/{eventId}/segments/{segmentId}/edit")
    public String editSegment(@PathVariable("eventId") Long eventId,
                              @PathVariable("segmentId") Long segmentId,
                              @ModelAttribute EventSegment segment,
                              HttpSession session) {
        Event event = findOwnedEvent(eventId, session);
        if (event == null) {
            return redirectForUnauthorizedSegmentAccess(session);
        }

        EventSegment existingSegment = eventSegmentRepository.findById(segmentId).orElse(null);
        if (existingSegment == null || !event.getId().equals(existingSegment.getEventId())) {
            return "redirect:/events/" + eventId;
        }

        existingSegment.setName(segment.getName());
        existingSegment.setDescription(segment.getDescription());
        existingSegment.setFee(segment.getFee());
        existingSegment.setTeamSize(segment.getTeamSize());
        existingSegment.setMinTeamSize(segment.getMinTeamSize());
        existingSegment.setMaxTeamSize(segment.getMaxTeamSize());
        existingSegment.setCapacity(segment.getCapacity());
        existingSegment.setRegistrationLink(segment.getRegistrationLink());
        existingSegment.setRegistrationDeadline(segment.getRegistrationDeadline());
        eventSegmentRepository.save(existingSegment);

        return "redirect:/events/" + eventId;
    }

    @PostMapping("/events/{eventId}/segments/{segmentId}/delete")
    public String deleteSegment(@PathVariable("eventId") Long eventId,
                                @PathVariable("segmentId") Long segmentId,
                                HttpSession session) {
        Event event = findOwnedEvent(eventId, session);
        if (event == null) {
            return redirectForUnauthorizedSegmentAccess(session);
        }

        EventSegment segment = eventSegmentRepository.findById(segmentId).orElse(null);
        if (segment != null && event.getId().equals(segment.getEventId())) {
            eventSegmentRepository.delete(segment);
        }

        return "redirect:/events/" + eventId;
    }

    private Event findOwnedEvent(Long eventId, HttpSession session) {
        User loggedInUser = session == null ? null : (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null || !ORGANIZER_ROLE.equals(loggedInUser.getRole())) {
            return null;
        }

        Event event = eventRepository.findById(eventId).orElse(null);
        if (event == null || loggedInUser.getEmail() == null || !loggedInUser.getEmail().equals(event.getOrganizerEmail())) {
            return null;
        }

        return event;
    }

    private String redirectForUnauthorizedSegmentAccess(HttpSession session) {
        User loggedInUser = session == null ? null : (User) session.getAttribute("loggedInUser");
        return loggedInUser == null ? "redirect:/auth/login" : "redirect:/";
    }

    private String redirectForUnauthorizedEventAccess(HttpSession session, RedirectAttributes redirectAttributes) {
        User loggedInUser = session == null ? null : (User) session.getAttribute("loggedInUser");
        if (loggedInUser == null) {
            return "redirect:/auth/login";
        }
        redirectAttributes.addAttribute("error", "You can only manage your own events.");
        return "redirect:/organizer/dashboard";
    }

    private String saveEventImage(MultipartFile file) {
        try {
            File directory = new File(UPLOAD_DIR);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            String uniqueFilename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path path = Paths.get(UPLOAD_DIR + uniqueFilename);
            Files.write(path, file.getBytes());
            return "/uploads/" + uniqueFilename;
        } catch (IOException e) {
            e.printStackTrace();
            return "https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=800";
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String resolveCategory(String selectedCategory, String newCategory) {
        String category = isBlank(newCategory) ? selectedCategory : newCategory.trim();
        if (isBlank(category)) {
            return selectedCategory;
        }
        if (eventCategoryRepository.findByNameIgnoreCase(category) == null) {
            eventCategoryRepository.save(new EventCategory(category));
        }
        return category.trim();
    }

    private void createEventNotifications(Event event) {
        if (event == null || event.getId() == null) {
            return;
        }
        if (event.getStatus() != null && !"PUBLISHED".equals(event.getStatus())) {
            return;
        }

        for (User student : userRepository.findByRole("STUDENT")) {
            Notification notification = new Notification(
                    student.getEmail(),
                    "New event published",
                    event.getTitle() + " is now live on Mycellium.",
                    event.getId()
            );
            notificationRepository.save(notification);
        }
    }
}
