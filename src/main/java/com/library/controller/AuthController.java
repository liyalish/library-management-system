package com.library.controller;

import com.library.exception.ServiceException;
import com.library.model.RegistrationForm;
import com.library.model.User;
import com.library.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Handles user authentication: login, registration, and logout.
 * The logged-in user is stored in the HTTP session.
 */
@Controller
public class AuthController {

    private final UserService userService;

    /**
     * Creates the controller with the user service (constructor injection).
     *
     * @param userService the user service
     */
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Shows the login form.
     *
     * @return the login view name
     */
    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    /**
     * Processes a login attempt. On success stores the user in session and redirects
     * to the book list; on failure returns to the login form with an error flag.
     *
     * @param username the submitted username
     * @param password the submitted password
     * @param session  the HTTP session
     * @param model    the view model
     * @return a redirect on success, or the login view on failure
     */
    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {
        try {
            User user = userService.authenticate(username, password);
            session.setAttribute("currentUser", user);
            return "redirect:/books";
        } catch (ServiceException e) {
            model.addAttribute("loginError", true);
            return "login";
        }
    }

    /**
     * Shows the registration form with an empty form-backing object.
     *
     * @param model the view model
     * @return the register view name
     */
    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("registrationForm", new RegistrationForm());
        return "register";
    }

    /**
     * Processes a registration. The form is validated server-side via Bean Validation;
     * if there are errors they are shown on the form. On success the user is logged in.
     *
     * @param form    the validated registration form
     * @param result  the binding/validation result
     * @param session the HTTP session
     * @param model   the view model
     * @return the register view on validation error, otherwise a redirect to books
     */
    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registrationForm") RegistrationForm form,
                           BindingResult result,
                           HttpSession session,
                           Model model) {
        if (result.hasErrors()) {
            return "register";
        }
        try {
            User user = userService.register(form.getUsername(), form.getPassword(),
                    form.getFullName(), form.getEmail());
            session.setAttribute("currentUser", user);
            return "redirect:/books";
        } catch (ServiceException e) {
            model.addAttribute("registerError", e.getMessage());
            return "register";
        }
    }

    /**
     * Logs the current user out by invalidating the session.
     *
     * @param session the HTTP session
     * @return a redirect to the login page
     */
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}