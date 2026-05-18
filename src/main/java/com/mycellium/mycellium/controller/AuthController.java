package com.mycellium.mycellium.controller;

import com.mycellium.mycellium.model.User;
import com.mycellium.mycellium.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller // 1. Make sure this says @Controller, NOT @RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    // 2. THIS IS WHAT TELLS SPRING BOOT HOW TO DISPLAY THE LOGIN PAGE WHEN CLICKED
    @GetMapping("/login")
    public String showLoginPage(Model model) {
        model.addAttribute("user", new User());
        return "login"; // Looks for login.html
    }

    // 3. THIS IS WHAT TELLS SPRING BOOT HOW TO DISPLAY THE SIGNUP PAGE WHEN CLICKED
    @GetMapping("/signup")
    public String showSignupPage(Model model) {
        model.addAttribute("user", new User());
        return "register"; // Looks for register.html
    }

    // 4. These process the actual form submissions
    @PostMapping("/signup")
    public String signup(@ModelAttribute("user") User user, Model model) {
        if(userRepository.findByEmail(user.getEmail()) != null) {
            model.addAttribute("error", "Email already exists");
            return "register";
        }
        userRepository.save(user);
        return "redirect:/auth/login?success=true";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute("user") User user, Model model) {
        User existingUser = userRepository.findByEmail(user.getEmail());
        if(existingUser == null || !existingUser.getPassword().equals(user.getPassword())) {
            model.addAttribute("error", "Invalid email or password");
            return "login";
        }
        return "redirect:/"; // Redirects to homepage on successful login
    }
}