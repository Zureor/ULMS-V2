package com.ulms.service;

import com.ulms.dto.request.BookRequest;
import com.ulms.model.Book;
import com.ulms.model.User;
import com.ulms.model.enums.UserRole;
import com.ulms.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
public class BookServiceTest {

    @Autowired
    private BookService bookService;

    @Autowired
    private UserRepository userRepository;

    private Long adminId;

    @BeforeEach
    void setUp() {
        User admin = new User();
        admin.setUsername("admin_test");
        admin.setPassword("encoded");
        admin.setFullName("Admin Test");
        admin.setEmail("admin_test@test.com");
        admin.setRole(UserRole.ADMIN);
        admin.setActive(true);
        admin = userRepository.save(admin);
        adminId = admin.getId();
    }

    @Test
    void shouldCreateBook() {
        BookRequest req = new BookRequest();
        req.setIsbn("978-TEST-001");
        req.setTitle("Test Book Title");
        req.setAuthor("Test Author");
        req.setCategory("Programming");
        req.setTotalCopies(5);

        Book book = bookService.createBook(req, adminId);

        assertThat(book.getId()).isNotNull();
        assertThat(book.getTitle()).isEqualTo("Test Book Title");
        assertThat(book.getAvailableCopies()).isEqualTo(5);
    }

    @Test
    void shouldNotDuplicateIsbn() {
        BookRequest req = new BookRequest();
        req.setIsbn("978-DUPE");
        req.setTitle("Book 1");
        req.setAuthor("Author");
        req.setCategory("Programming");
        req.setTotalCopies(1);

        bookService.createBook(req, adminId);

        assertThrows(RuntimeException.class, () -> bookService.createBook(req, adminId));
    }

    @Test
    void shouldSearchBooks() {
        BookRequest req = new BookRequest();
        req.setIsbn("978-SEARCH");
        req.setTitle("Unique Searchable Title");
        req.setAuthor("Search Author");
        req.setCategory("Programming");
        req.setTotalCopies(2);
        bookService.createBook(req, adminId);

        List<Book> results = bookService.getAllBooks("Searchable", "",
                org.springframework.data.domain.PageRequest.of(0, 10));

        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getTitle()).containsIgnoringCase("searchable");
    }
}