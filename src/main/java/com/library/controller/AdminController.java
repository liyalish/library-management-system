package com.library.controller;

import com.library.exception.ServiceException;
import com.library.service.RequestService;
import com.library.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminController {
    private static final int PAGE_SIZE = 5;

    private final UserService userService;
    private final RequestService requestService;

    public AdminController(UserService userService, RequestService requestService) {
        this.userService = userService;
        this.requestService = requestService;
    }

    @GetMapping("/admin/users")
    public String users(@RequestParam(defaultValue = "1") int page, Model model) {
        int total = userService.getUserCount();
        int totalPages = Math.max(1, (int) Math.ceil((double) total / PAGE_SIZE));

        if (page < 1) {
            page = 1;
        }

        if (page > totalPages) {
            page = totalPages;
        }

        model.addAttribute("users", userService.getUsers(page, PAGE_SIZE));
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);

        return "admin-users";
    }

    @PostMapping("/admin/users/librarians")
    public String createLibrarian(@RequestParam String username,
                                  @RequestParam String password,
                                  @RequestParam String fullName,
                                  @RequestParam String email,
                                  RedirectAttributes ra) {
        try {
            userService.createLibrarian(username, password, fullName, email);
            ra.addFlashAttribute("message", "Librarian account created");
        } catch (ServiceException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/users";
    }

    @PostMapping("/admin/users/{id}/block")
    public String setBlocked(@PathVariable int id,
                             @RequestParam boolean blocked,
                             RedirectAttributes ra) {
        try {
            userService.setBlocked(id, blocked);
            ra.addFlashAttribute("message", "User #" + id + (blocked ? " blocked" : " unblocked"));
        } catch (ServiceException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/users";
    }

    @PostMapping("/admin/users/{id}/delete")
    public String delete(@PathVariable int id, RedirectAttributes ra) {
        try {
            userService.deleteUser(id);
            ra.addFlashAttribute("message", "Librarian #" + id + " deleted");
        } catch (ServiceException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/users";
    }

    @GetMapping("/admin/requests")
    public String viewRequests(Model model) {
        model.addAttribute("requests", requestService.getAllRequests());
        return "librarian-requests";
    }
}