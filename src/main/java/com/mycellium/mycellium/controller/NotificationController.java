package com.mycellium.mycellium.controller;

import com.mycellium.mycellium.model.Notification;
import com.mycellium.mycellium.model.User;
import com.mycellium.mycellium.repository.NotificationRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class NotificationController {

    @Autowired
    private NotificationRepository notificationRepository;

    @PostMapping("/student/notifications/{id}/read")
    public String markRead(@PathVariable("id") Long id, HttpSession session) {
        User loggedInUser = getStudent(session);
        if (loggedInUser == null) {
            return "redirect:/auth/login";
        }

        Notification notification = notificationRepository.findById(id).orElse(null);
        if (notification != null && loggedInUser.getEmail().equals(notification.getUserEmail())) {
            notification.setReadStatus(true);
            notificationRepository.save(notification);
            if (notification.getEventId() != null) {
                return "redirect:/events/" + notification.getEventId();
            }
        }

        return "redirect:/student/dashboard";
    }

    @PostMapping("/student/notifications/read-all")
    public String markAllRead(HttpSession session) {
        User loggedInUser = getStudent(session);
        if (loggedInUser == null) {
            return "redirect:/auth/login";
        }

        notificationRepository.markAllRead(loggedInUser.getEmail());
        return "redirect:/student/dashboard";
    }

    private User getStudent(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object user = session.getAttribute("loggedInUser");
        if (!(user instanceof User loggedInUser) || !"STUDENT".equals(loggedInUser.getRole())) {
            return null;
        }
        return loggedInUser;
    }
}
