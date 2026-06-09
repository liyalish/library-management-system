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

/**
 * Reader-facing controller for the borrowing workflow: submitting book requests,
 * viewing one's own requests, and cancelling pending ones. All endpoints live under
 * /requests and are protected by the authorization interceptor (login required).
 */
@Controller
public class RequestController {

    private final RequestService requestService;

    /**
     * Creates the controller with the request service (constructor injection).
     *
     * @param requestService the request service
     */
    public RequestController(RequestService requestService) {
        this.requestService = requestService;
    }

    /**
     * Submits a request for a book on behalf of the logged-in reader.
     *
     * @param bookId      the requested book id
     * @param requestType "HOME" or "READING_ROOM"
     * @param session     the HTTP session (provides the current user)
     * @return a redirect to the reader's request list
     */
    @PostMapping("/requests/create")
    public String create(@RequestParam int bookId,
                         @RequestParam String requestType,
                         HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        requestService.submitRequest(user.getUserId(), bookId, requestType);
        return "redirect:/requests";
    }

    /**
     * Shows the logged-in reader their own requests.
     *
     * @param session the HTTP session
     * @param model   the view model
     * @return the my-requests view name
     */
    @GetMapping("/requests")
    public String myRequests(HttpSession session, Model model) {
        User user = (User) session.getAttribute("currentUser");
        model.addAttribute("requests", requestService.getReaderRequests(user.getUserId()));
        return "my-requests";
    }

    /**
     * Cancels one of the reader's pending requests.
     *
     * @param id      the request id
     * @param session the HTTP session
     * @param model   the view model
     * @return a redirect to the reader's request list
     */
    @PostMapping("/requests/{id}/cancel")
    public String cancel(@PathVariable int id, HttpSession session, Model model) {
        User user = (User) session.getAttribute("currentUser");
        try {
            requestService.cancelRequest(id, user.getUserId());
        } catch (ServiceException e) {
            model.addAttribute("error", e.getMessage());
        }
        return "redirect:/requests";
    }
}