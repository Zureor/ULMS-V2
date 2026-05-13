package com.ulms.controller;

import com.ulms.dto.request.BookRequest;
import com.ulms.model.Book;
import com.ulms.model.User;
import com.ulms.service.BookService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;
    private final com.ulms.service.ReservationService reservationService;

    public BookController(BookService bookService, com.ulms.service.ReservationService reservationService) {
        this.bookService = bookService;
        this.reservationService = reservationService;
    }

    @GetMapping
    public String listBooks(@RequestParam(defaultValue = "") String query,
                             @RequestParam(defaultValue = "") String category,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "10") int size,
                             Authentication auth, Model model) {
        User user = (User) auth.getPrincipal();
        model.addAttribute("role", user.getRole().name());
        model.addAttribute("query", query);
        model.addAttribute("category", category);

        List<Book> books = bookService.getAllBooks(query, category,
                PageRequest.of(page, size, Sort.by("title").ascending()));
        model.addAttribute("books", books);
        model.addAttribute("categories", java.util.List.of("Programming", "Literature", "Science", "History", "Art", "General"));

        if ("STUDENT".equals(user.getRole().name())) {
            List<Long> reservedBookIds = reservationService.getUserReservations(user.getId())
                    .stream()
                    .filter(r -> r.getStatus() == com.ulms.model.enums.ReservationStatus.PENDING || 
                                 r.getStatus() == com.ulms.model.enums.ReservationStatus.ISSUED ||
                                 r.getStatus() == com.ulms.model.enums.ReservationStatus.APPROVED)
                    .map(r -> r.getBook().getId())
                    .toList();
            model.addAttribute("reservedBookIds", reservedBookIds);
        }

        return "STUDENT".equals(user.getRole().name()) ? "student/books" : "admin/books";
    }

    @GetMapping("/{id}")
    public String bookDetail(@PathVariable Long id, Model model) {
        Book book = bookService.getBookById(id);
        model.addAttribute("book", book);
        return "books/detail";
    }

    @GetMapping("/new")
    public String addBookForm(Model model) {
        model.addAttribute("bookRequest", new BookRequest());
        return "admin/books-form";
    }

    @PostMapping
    public String createBook(@Valid @ModelAttribute BookRequest request,
                              BindingResult result, Authentication auth) {
        if (result.hasErrors()) return "admin/books-form";
        User user = (User) auth.getPrincipal();
        bookService.createBook(request, user.getId());
        return "redirect:/api/books";
    }

    @GetMapping("/edit/{id}")
    public String editBookForm(@PathVariable Long id, Model model) {
        Book book = bookService.getBookById(id);
        model.addAttribute("bookRequest", toBookRequest(book));
        model.addAttribute("editMode", true);
        return "admin/books-form";
    }

    @PostMapping("/update/{id}")
    public String updateBook(@PathVariable Long id,
                              @Valid @ModelAttribute BookRequest request,
                              BindingResult result, Authentication auth) {
        if (result.hasErrors()) return "admin/books-form";
        User user = (User) auth.getPrincipal();
        bookService.updateBook(id, request, user.getId());
        return "redirect:/api/books";
    }

    @PostMapping("/delete/{id}")
    public String deleteBook(@PathVariable Long id, Authentication auth) {
        User user = (User) auth.getPrincipal();
        bookService.deleteBook(id, user.getId());
        return "redirect:/api/books";
    }

    @GetMapping("/api/list")
    @ResponseBody
    public ResponseEntity<List<Book>> getAllBooksApi(
            @RequestParam(defaultValue = "") String query,
            @RequestParam(defaultValue = "") String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        List<Book> books = bookService.getAllBooks(query, category,
                PageRequest.of(page, size, Sort.by("title").ascending()));
        return ResponseEntity.ok(books);
    }

    @GetMapping("/api/{id}")
    @ResponseBody
    public ResponseEntity<Book> getBookApi(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.getBookById(id));
    }

    @PostMapping("/api/create")
    @ResponseBody
    public ResponseEntity<?> createBookApi(@RequestBody @Valid BookRequest request,
                                            Authentication auth) {
        User user = (User) auth.getPrincipal();
        Book book = bookService.createBook(request, user.getId());
        return ResponseEntity.ok(book);
    }

    @PutMapping("/api/update/{id}")
    @ResponseBody
    public ResponseEntity<?> updateBookApi(@PathVariable Long id,
                                            @RequestBody @Valid BookRequest request,
                                            Authentication auth) {
        User user = (User) auth.getPrincipal();
        Book book = bookService.updateBook(id, request, user.getId());
        return ResponseEntity.ok(book);
    }

    @DeleteMapping("/api/delete/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteBookApi(@PathVariable Long id, Authentication auth) {
        User user = (User) auth.getPrincipal();
        bookService.deleteBook(id, user.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/bulk-import")
    @ResponseBody
    public ResponseEntity<?> bulkImport(@RequestBody java.util.List<BookRequest> books,
                                         Authentication auth) {
        User user = (User) auth.getPrincipal();
        java.util.List<Book> imported = bookService.bulkImport(books, user.getId());
        return ResponseEntity.ok(Map.of(
                "message", "Imported " + imported.size() + " books",
                "imported", imported.size()
        ));
    }

    private BookRequest toBookRequest(Book book) {
        BookRequest req = new BookRequest();
        req.setIsbn(book.getIsbn());
        req.setTitle(book.getTitle());
        req.setAuthor(book.getAuthor());
        req.setPublisher(book.getPublisher());
        req.setEdition(book.getEdition());
        req.setCategory(book.getCategory());
        req.setTotalCopies(book.getTotalCopies());
        req.setDescription(book.getDescription());
        req.setCoverImageUrl(book.getCoverImageUrl());
        return req;
    }
}