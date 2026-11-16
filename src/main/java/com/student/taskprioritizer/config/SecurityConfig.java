package com.student.taskprioritizer.config;

import com.student.taskprioritizer.security.AppUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(
            AppUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            DaoAuthenticationProvider authenticationProvider) throws Exception {
        http
                .authenticationProvider(authenticationProvider)
                .authorizeHttpRequests(auth -> auth
                        // Static resources & login page — public
                        .requestMatchers("/login", "/style.css", "/css/**", "/favicon.ico").permitAll()
                        // Student SPA (served at / as static HTML)
                        .requestMatchers("/", "/index.html", "/app.js").authenticated()
                        // Student REST APIs
                        .requestMatchers("/api/tasks/**").hasRole("STUDENT")
                        // Admin Thymeleaf pages + REST
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        // Faculty Thymeleaf pages + REST
                        .requestMatchers("/faculty/**").hasRole("FACULTY")
                        // Everything else requires auth
                        .anyRequest().authenticated()
                )
                // Form login — Thymeleaf login page
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler((request, response, authentication) -> {
                            // Redirect to role-appropriate dashboard after login
                            var authorities = authentication.getAuthorities();
                            String target = "/";
                            for (var ga : authorities) {
                                String role = ga.getAuthority();
                                if ("ROLE_ADMIN".equals(role)) { target = "/admin/dashboard"; break; }
                                if ("ROLE_FACULTY".equals(role)) { target = "/faculty/dashboard"; break; }
                            }
                            response.sendRedirect(target);
                        })
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )
                // CSRF is ON (Spring default). The student SPA sends mutating
                // requests via fetch, so it needs the CSRF token. We expose it
                // as a cookie that JavaScript can read.
                .csrf(csrf -> csrf
                        .csrfTokenRepository(
                                org.springframework.security.web.csrf.CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler())
                );

        return http.build();
    }
}
