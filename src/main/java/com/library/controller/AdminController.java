package com.library.controller;

import com.library.exception.ServiceException;
import com.library.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Administrator-facing controller for managing user accounts: listing users with
 * pagination, blocking/unblocking, and deleting. All endpoints live under /admin and
 * require the ADMIN role, enforced by the authorization interceptor.
 */
@Controller
public class AdminController {

    private static final int PAGE_SIZE = 5;

    private final UserService userService;

    /**
     * Creates the controller with the user service (constructor injection).
     *
     * @param userService the user service
     */
    public AdminController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Shows a paginated list of all users.
     *
     * @param page  1-based page number (defaults to 1)
     * @param model the view model
     * @return the admin users view name
     */
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

    /**
     * Blocks or unblocks a user account.
     *
     * @param id      the user id
     * @param blocked the new blocked state
     * @param ra      redirect attributes for flash messages
     * @return a redirect to the user list
     */
    @PostMapping("/admin/users/{id}/block")
    public String setBlocked(@PathVariable int id,
                             @RequestParam boolean blocked,
                             RedirectAttributes ra) {
        userService.setBlocked(id, blocked);
        ra.addFlashAttribute("message", "User #" + id + (blocked ? " blocked" : " unblocked"));
        return "redirect:/admin/users";
    }

    /**
     * Deletes a user account.
     *
     * @param id the user id
     * @param ra redirect attributes for flash messages
     * @return a redirect to the user list
     */
    @PostMapping("/admin/users/{id}/delete")
    public String delete(@PathVariable int id, RedirectAttributes ra) {
        try {
            userService.deleteUser(id);
            ra.addFlashAttribute("message", "User #" + id + " deleted");
        } catch (ServiceException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/users";
    }
}