package com.library.controller;

import com.library.exception.ServiceException;
import com.library.service.RequestService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class LibrarianController {
    private final RequestService requestService;

    public LibrarianController(RequestService requestService) {
        this.requestService = requestService;
    }

    @GetMapping("/librarian/requests")
    public String allRequests(Model model) {
        model.addAttribute("requests", requestService.getAllRequests());
        return "librarian-requests";
    }

    @PostMapping("/librarian/requests/{id}/issue")
    public String issue(@PathVariable int id, @RequestParam String returnDate, RedirectAttributes ra) {
        try {
            requestService.issueBook(id, returnDate);
            ra.addFlashAttribute("message", "Book issued for request #" + id);
        } catch (ServiceException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/librarian/requests";
    }
    
    @PostMapping("/librarian/requests/{id}/return")
    public String returnBook(@PathVariable int id, RedirectAttributes ra) {
        try {
            requestService.returnBook(id);
            ra.addFlashAttribute("message", "Book returned for request #" + id);
        } catch (ServiceException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/librarian/requests";
    }
}