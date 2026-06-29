package com.mycellium.mycellium.controller;

import com.mycellium.mycellium.model.User;
import com.mycellium.mycellium.repository.NotificationRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class NotificationModelAdvice {

    @Autowired
    private NotificationRepository notificationRepository;

    @ModelAttribute
    public void addNotifications(Model model, HttpSession session) {
        if (session == null) {
            return;
        }

        Object user = session.getAttribute("loggedInUser");
        if (!(user instanceof User loggedInUser) || !"STUDENT".equals(loggedInUser.getRole())) {
            return;
        }

        model.addAttribute("recentNotifications", notificationRepository.findTop5ByUserEmailOrderByCreatedAtDesc(loggedInUser.getEmail()));
        model.addAttribute("unreadNotificationsCount", notificationRepository.countByUserEmailAndReadStatus(loggedInUser.getEmail(), false));
    }
}
