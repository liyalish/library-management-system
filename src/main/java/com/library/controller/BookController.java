package com.library.controller;

import com.library.model.Book;
import com.library.service.BookService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Handles the book catalog: listing with search and pagination, plus create, edit,
 * and delete operations.
 */
@Controller
public class BookController {

    private static final int PAGE_SIZE = 5;

    private final BookService bookService;

    /**
     * Creates the controller with the book service (constructor injection).
     *
     * @param bookService the book service
     */
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    /**
     * Shows a paginated, optionally filtered list of books.
     *
     * @param search optional title search term
     * @param page   1-based page number (defaults to 1)
     * @param model  the view model
     * @return the books view name
     */
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

    /**
     * Shows the form to add a new book.
     *
     * @param model the view model
     * @return the book form view name
     */
    @GetMapping("/books/new")
    public String createForm(Model model) {
        model.addAttribute("book", new Book());
        return "book-form";
    }

    /**
     * Shows the form to edit an existing book.
     *
     * @param id    the book id
     * @param model the view model
     * @return the book form view name
     */
    @GetMapping("/books/{id}/edit")
    public String editForm(@PathVariable int id, Model model) {
        model.addAttribute("book", bookService.getBook(id));
        return "book-form";
    }

    /**
     * Saves a book — creates it when id is 0, otherwise updates it.
     *
     * @param book the submitted book data
     * @return a redirect to the book list
     */
    @PostMapping("/books/save")
    public String save(Book book) {
        if (book.getBookId() == 0) {
            bookService.addBook(book);
        } else {
            bookService.updateBook(book);
        }
        return "redirect:/books";
    }

    /**
     * Deletes a book and returns to the list.
     *
     * @param id the book id to delete
     * @return a redirect to the book list
     */
    @PostMapping("/books/{id}/delete")
    public String delete(@PathVariable int id) {
        bookService.deleteBook(id);
        return "redirect:/books";
    }
}