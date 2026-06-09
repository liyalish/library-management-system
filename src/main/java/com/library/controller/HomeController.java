package com.library.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Redirects the application root to the book catalog.
 */
@Controller
public class HomeController {

    /**
     * Redirects "/" to the books page.
     *
     * @return a redirect to /books
     */
    @GetMapping("/")
    public String home() {
        return "redirect:/books";
    }
}