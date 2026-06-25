package com.library.config;

import com.library.dao.UserDao;
import com.library.model.User;
import com.library.security.CustomUserDetailsService;
import jakarta.servlet.ServletException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private final CustomUserDetailsService userDetailsService;
    private final UserDao userDao;

    public SecurityConfig(CustomUserDetailsService userDetailsService,
                          UserDao userDao) {
        this.userDetailsService = userDetailsService;
        this.userDao = userDao;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();

        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());

        return provider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(Customizer.withDefaults())
                .authenticationProvider(authenticationProvider())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                new AntPathRequestMatcher("/", "GET"),
                                new AntPathRequestMatcher("/login"),
                                new AntPathRequestMatcher("/register"),
                                new AntPathRequestMatcher("/access-denied"),
                                new AntPathRequestMatcher("/css/**"),
                                new AntPathRequestMatcher("/js/**"),
                                new AntPathRequestMatcher("/favicon.ico")
                        ).permitAll()

                        .requestMatchers(new AntPathRequestMatcher("/books", "GET")).permitAll()

                        .requestMatchers(new AntPathRequestMatcher("/books/new", "GET")).hasRole("LIBRARIAN")
                        .requestMatchers(new AntPathRequestMatcher("/books/*/edit", "GET")).hasRole("LIBRARIAN")
                        .requestMatchers(new AntPathRequestMatcher("/books/save", "POST")).hasRole("LIBRARIAN")
                        .requestMatchers(new AntPathRequestMatcher("/books/*/delete", "POST")).hasRole("LIBRARIAN")

                        .requestMatchers(new AntPathRequestMatcher("/requests/**")).hasRole("READER")
                        .requestMatchers(new AntPathRequestMatcher("/account/**")).hasRole("READER")

                        .requestMatchers(new AntPathRequestMatcher("/librarian/**")).hasRole("LIBRARIAN")
                        .requestMatchers(new AntPathRequestMatcher("/admin/**")).hasRole("ADMIN")

                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler((request, response, authentication) -> {
                            User user = userDao.findByUsername(authentication.getName())
                                    .orElseThrow(() -> new ServletException("Authenticated user not found"));

                            request.getSession().setAttribute("currentUser", user);

                            response.sendRedirect(request.getContextPath() + "/books");
                        })
                        .failureHandler((request, response, exception) -> {
                            if (exception instanceof LockedException) {
                                response.sendRedirect(request.getContextPath() + "/login?blocked=true");
                            } else {
                                response.sendRedirect(request.getContextPath() + "/login?error=true");
                            }
                        })
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .exceptionHandling(exception -> exception
                        .accessDeniedPage("/access-denied")
                );

        return httpSecurity.build();
    }
}