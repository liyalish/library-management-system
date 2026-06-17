package com.library.controller;

import com.library.exception.ServiceException;
import com.library.model.User;
import com.library.service.RequestService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RequestController {
    private final RequestService requestService;

    public RequestController(RequestService requestService) {
        this.requestService = requestService;
    }

    @PostMapping("/requests/create")
    public String create(@RequestParam int bookId,
                         @RequestParam String requestType,
                         HttpSession session,
                         RedirectAttributes ra) {
        User user = (User) session.getAttribute("currentUser");

        try {
            requestService.submitRequest(user.getUserId(), bookId, requestType);
            ra.addFlashAttribute("message", "Request submitted successfully");
        } catch (ServiceException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/requests";
    }

    @GetMapping("/requests")
    public String myRequests(HttpSession session, Model model) {
        User user = (User) session.getAttribute("currentUser");
        model.addAttribute("requests", requestService.getReaderRequests(user.getUserId()));
        return "my-requests";
    }

    @PostMapping("/requests/{id}/cancel")
    public String cancel(@PathVariable int id,
                         HttpSession session,
                         RedirectAttributes ra) {
        User user = (User) session.getAttribute("currentUser");

        try {
            requestService.cancelRequest(id, user.getUserId());
            ra.addFlashAttribute("message", "Request cancelled");
        } catch (ServiceException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/requests";
    }

    @PostMapping("/requests/{id}/return")
    public String requestReturn(@PathVariable int id,
                                HttpSession session,
                                RedirectAttributes ra) {
        User user = (User) session.getAttribute("currentUser");

        try {
            requestService.requestReturn(id, user.getUserId());
            ra.addFlashAttribute("message", "Return request sent to librarian");
        } catch (ServiceException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/requests";
    }
}