package com.student.taskprioritizer.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    @GetMapping("/")
    public String index(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            if (isAdmin) {
                return "redirect:/admin/dashboard";
            }
            boolean isFaculty = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_FACULTY"));
            if (isFaculty) {
                return "redirect:/faculty/dashboard";
            }
        }
        return "forward:/index.html";
    }

    @GetMapping("/login")
    public String login(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            if (isAdmin) {
                return "redirect:/admin/dashboard";
            }
            boolean isFaculty = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_FACULTY"));
            if (isFaculty) {
                return "redirect:/faculty/dashboard";
            }
            return "redirect:/";
        }
        return "login";
    }
}
