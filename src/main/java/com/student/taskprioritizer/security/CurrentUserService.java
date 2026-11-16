package com.student.taskprioritizer.security;

import com.student.taskprioritizer.model.Role;
import com.student.taskprioritizer.model.User;
import com.student.taskprioritizer.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User requireUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Not authenticated");
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof AppUserDetails details) {
            return userRepository.findById(details.getUser().getId())
                    .orElse(details.getUser());
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseGet(() -> {
                    Role role = authentication.getAuthorities().stream()
                            .map(a -> a.getAuthority().replace("ROLE_", ""))
                            .filter(r -> r.equals("ADMIN") || r.equals("FACULTY") || r.equals("STUDENT"))
                            .map(Role::valueOf)
                            .findFirst()
                            .orElse(Role.STUDENT);
                    User fallback = new User();
                    fallback.setId(UUID.randomUUID());
                    fallback.setName(email);
                    fallback.setEmail(email);
                    fallback.setRole(role);
                    return fallback;
                });
    }

    public void requireRole(Role role) {
        User user = requireUser();
        if (user.getRole() != role) {
            throw new org.springframework.security.access.AccessDeniedException("Requires " + role);
        }
    }
}
