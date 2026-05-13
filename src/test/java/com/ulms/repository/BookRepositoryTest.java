package com.ulms.repository;

import com.ulms.model.Book;
import com.ulms.model.User;
import com.ulms.model.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("dev")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class BookRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSaveAndFindBook() {
        Book book = new Book();
        book.setIsbn("978-001");
        book.setTitle("Test Book");
        book.setAuthor("Test Author");
        book.setCategory("Programming");
        book.setTotalCopies(5);
        book.setAvailableCopies(5);

        Book saved = bookRepository.save(book);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTitle()).isEqualTo("Test Book");
    }

    @Test
    void shouldSearchBooksByQuery() {
        Book book = new Book();
        book.setIsbn("978-002");
        book.setTitle("Spring Boot in Action");
        book.setAuthor("Craig Walls");
        book.setCategory("Programming");
        book.setTotalCopies(3);
        book.setAvailableCopies(3);
        bookRepository.save(book);

        List<Book> results = bookRepository.searchBooks("Spring", "",
                org.springframework.data.domain.PageRequest.of(0, 10));

        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getTitle()).containsIgnoringCase("Spring");
    }

    @Test
    void shouldFilterByCategory() {
        Book programming = new Book();
        programming.setIsbn("978-003");
        programming.setTitle("Java Guide");
        programming.setAuthor("A");
        programming.setCategory("Programming");
        programming.setTotalCopies(1);
        programming.setAvailableCopies(1);

        Book literature = new Book();
        literature.setIsbn("978-004");
        literature.setTitle("Poems");
        literature.setAuthor("B");
        literature.setCategory("Literature");
        literature.setTotalCopies(1);
        literature.setAvailableCopies(1);

        bookRepository.saveAll(List.of(programming, literature));

        List<Book> results = bookRepository.searchBooks("", "Programming",
                org.springframework.data.domain.PageRequest.of(0, 10));

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getCategory()).isEqualTo("Programming");
    }

    @Test
    void shouldCheckExistsByIsbn() {
        Book book = new Book();
        book.setIsbn("978-005");
        book.setTitle("Book1");
        book.setAuthor("A");
        book.setCategory("General");
        book.setTotalCopies(1);
        book.setAvailableCopies(1);
        bookRepository.save(book);

        assertThat(bookRepository.existsByIsbn("978-005")).isTrue();
        assertThat(bookRepository.existsByIsbn("978-999")).isFalse();
    }
}