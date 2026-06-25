package com.library.controller;

import com.library.exception.ServiceException;
import com.library.model.RegistrationForm;
import com.library.model.User;
import com.library.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("registrationForm", new RegistrationForm());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registrationForm") RegistrationForm form,
                           BindingResult result,
                           Model model) {
        if (result.hasErrors()) {
            return "register";
        }

        try {
            userService.register(
                    form.getUsername(),
                    form.getPassword(),
                    form.getFullName(),
                    form.getEmail()
            );

            return "redirect:/login?registered=true";
        } catch (ServiceException e) {
            model.addAttribute("registerError", e.getMessage());
            return "register";
        }
    }

    @PreAuthorize("hasRole('READER')")
    @PostMapping("/account/delete")
    public String deleteOwnAccount(HttpSession session,
                                   RedirectAttributes ra) {
        User user = (User) session.getAttribute("currentUser");

        try {
            userService.deleteOwnAccount(user.getUserId());
            session.invalidate();
            return "redirect:/login";
        } catch (ServiceException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/requests";
        }
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "error-403";
    }
}