package com.mycellium.mycellium.controller;

import com.mycellium.mycellium.model.User;
import com.mycellium.mycellium.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private static final String STUDENT_ROLE = "STUDENT";
    private static final String ORGANIZER_ROLE = "ORGANIZER";
    private static final int SESSION_TIMEOUT_SECONDS = 30 * 60;
    private static final PasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/login")
    public String showLoginPage(HttpSession session, Model model) {
        User loggedInUser = getLoggedInUser(session);
        if (loggedInUser != null) {
            return redirectForRole(loggedInUser);
        }
        model.addAttribute("user", new User());
        return "login";
    }

    @GetMapping("/register")
    public String showSignupPage(HttpSession session, Model model) {
        User loggedInUser = getLoggedInUser(session);
        if (loggedInUser != null) {
            return redirectForRole(loggedInUser);
        }
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute("user") User user,
                           @RequestParam("role") String role,
                           @RequestParam("confirmPassword") String confirmPassword,
                           Model model) {
        String email = user.getEmail() == null ? "" : user.getEmail().trim().toLowerCase();
        String password = user.getPassword() == null ? "" : user.getPassword();
        String normalizedRole = role == null ? "" : role.trim().toUpperCase();

        if (user.getName() == null || user.getName().trim().isEmpty()) {
            model.addAttribute("error", "Full name is required.");
            return "register";
        }
        if (email.isEmpty()) {
            model.addAttribute("error", "Email address is required.");
            return "register";
        }
        if (password.isBlank()) {
            model.addAttribute("error", "Password is required.");
            return "register";
        }
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Password and confirm password must match.");
            return "register";
        }
        if (!isAllowedRole(normalizedRole)) {
            model.addAttribute("error", "Please choose a valid account type.");
            return "register";
        }
        if (userRepository.findByEmail(email) != null) {
            model.addAttribute("error", "An account with this email already exists.");
            return "register";
        }

        user.setName(user.getName().trim());
        user.setEmail(email);
        user.setPassword(PASSWORD_ENCODER.encode(password));
        user.setRole(normalizedRole);
        userRepository.save(user);
        return "redirect:/auth/login?success=true";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute("user") User user,
                        HttpServletRequest request,
                        Model model) {
        String email = user.getEmail() == null ? "" : user.getEmail().trim().toLowerCase();
        String password = user.getPassword() == null ? "" : user.getPassword();
        User existingUser = userRepository.findByEmail(email);

        if (email.isEmpty() || password.isBlank()) {
            model.addAttribute("error", "Email and password are required.");
            return "login";
        }
        if (existingUser == null || !passwordMatches(password, existingUser.getPassword())) {
            model.addAttribute("error", "Invalid email or password");
            return "login";
        }
        if (!isAllowedRole(existingUser.getRole())) {
            model.addAttribute("error", "Your account role is not valid. Please contact support.");
            return "login";
        }
        if (!isHashedPassword(existingUser.getPassword())) {
            existingUser.setPassword(PASSWORD_ENCODER.encode(password));
            userRepository.save(existingUser);
        }

        HttpSession existingSession = request.getSession(false);
        if (existingSession != null) {
            existingSession.invalidate();
        }

        HttpSession newSession = request.getSession(true);
        newSession.setMaxInactiveInterval(SESSION_TIMEOUT_SECONDS);
        newSession.setAttribute("loggedInUser", toSessionUser(existingUser));

        return redirectForRole(existingUser);
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/";
    }

    private boolean passwordMatches(String rawPassword, String savedPassword) {
        if (savedPassword == null || savedPassword.isBlank()) {
            return false;
        }
        if (isHashedPassword(savedPassword)) {
            return PASSWORD_ENCODER.matches(rawPassword, savedPassword);
        }
        return savedPassword.equals(rawPassword);
    }

    private boolean isHashedPassword(String savedPassword) {
        return savedPassword != null
                && (savedPassword.startsWith("$2a$")
                || savedPassword.startsWith("$2b$")
                || savedPassword.startsWith("$2y$"));
    }

    private boolean isAllowedRole(String role) {
        return STUDENT_ROLE.equals(role) || ORGANIZER_ROLE.equals(role);
    }

    private User getLoggedInUser(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object user = session.getAttribute("loggedInUser");
        return user instanceof User ? (User) user : null;
    }

    private User toSessionUser(User user) {
        User sessionUser = new User();
        sessionUser.setId(user.getId());
        sessionUser.setName(user.getName());
        sessionUser.setEmail(user.getEmail());
        sessionUser.setRole(user.getRole());
        return sessionUser;
    }

    private String redirectForRole(User user) {
        if (ORGANIZER_ROLE.equals(user.getRole())) {
            return "redirect:/organizer/dashboard";
        }
        return "redirect:/";
    }
}
