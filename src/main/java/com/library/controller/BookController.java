package com.library.controller;

import com.library.model.Book;
import com.library.model.Role;
import com.library.model.User;
import com.library.service.BookService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class BookController {
    private static final int PAGE_SIZE = 5;
    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping("/books")
    public String list(@RequestParam(required = false) String search,
                       @RequestParam(defaultValue = "1") int page,
                       Model model) {
        int total = bookService.getBookCount(search);
        int totalPages = Math.max(1, (int) Math.ceil((double) total / PAGE_SIZE));

        if (page < 1) {
            page = 1;
        }

        if (page > totalPages) {
            page = totalPages;
        }

        model.addAttribute("books", bookService.getBooks(search, page, PAGE_SIZE));
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("search", search);

        return "books";
    }

    @GetMapping("/books/new")
    public String createForm(Model model, HttpSession session) {
        requireLibrarian(session);
        model.addAttribute("book", new Book());
        return "book-form";
    }

    @GetMapping("/books/{id}/edit")
    public String editForm(@PathVariable int id, Model model, HttpSession session) {
        requireLibrarian(session);
        model.addAttribute("book", bookService.getBook(id));
        return "book-form";
    }

    @PostMapping("/books/save")
    public String save(Book book, HttpSession session) {
        requireLibrarian(session);

        if (book.getBookId() == 0) {
            bookService.addBook(book);
        } else {
            bookService.updateBook(book);
        }

        return "redirect:/books";
    }

    @PostMapping("/books/{id}/delete")
    public String delete(@PathVariable int id, HttpSession session) {
        requireLibrarian(session);
        bookService.deleteBook(id);
        return "redirect:/books";
    }

    private void requireLibrarian(HttpSession session) {
        User user = (User) session.getAttribute("currentUser");

        if (user == null || user.getRole() != Role.LIBRARIAN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }
}