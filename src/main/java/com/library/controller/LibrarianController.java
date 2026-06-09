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

/**
 * Librarian-facing controller for processing book requests: viewing all requests,
 * issuing a book (assigns a copy and sets a return date), and marking a book returned.
 * All endpoints live under /librarian and require the LIBRARIAN (or ADMIN) role,
 * enforced by the authorization interceptor.
 */
@Controller
public class LibrarianController {

    private final RequestService requestService;

    /**
     * Creates the controller with the request service (constructor injection).
     *
     * @param requestService the request service
     */
    public LibrarianController(RequestService requestService) {
        this.requestService = requestService;
    }

    /**
     * Shows all requests in the system for the librarian to process.
     *
     * @param model the view model
     * @return the librarian requests view name
     */
    @GetMapping("/librarian/requests")
    public String allRequests(Model model) {
        model.addAttribute("requests", requestService.getAllRequests());
        return "librarian-requests";
    }

    /**
     * Issues a book for a pending request, assigning a copy and a return date.
     *
     * @param id         the request id
     * @param returnDate the due date (yyyy-MM-dd)
     * @param ra         redirect attributes for flash messages
     * @return a redirect to the request list
     */
    @PostMapping("/librarian/requests/{id}/issue")
    public String issue(@PathVariable int id,
                        @RequestParam String returnDate,
                        RedirectAttributes ra) {
        try {
            requestService.issueBook(id, returnDate);
            ra.addFlashAttribute("message", "Book issued for request #" + id);
        } catch (ServiceException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/librarian/requests";
    }

    /**
     * Marks an issued request as returned, freeing the copy.
     *
     * @param id the request id
     * @param ra redirect attributes for flash messages
     * @return a redirect to the request list
     */
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