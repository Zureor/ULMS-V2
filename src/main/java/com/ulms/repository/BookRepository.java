package com.ulms.repository;

import com.ulms.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    boolean existsByIsbn(String isbn);

    @Query("SELECT b FROM Book b WHERE " +
           "(:query IS NULL OR :query = '' OR " +
           "LOWER(b.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(b.author) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(b.isbn) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "AND (:category IS NULL OR :category = '' OR LOWER(b.category) = LOWER(:category))")
    List<Book> searchBooks(@Param("query") String query, @Param("category") String category,
                           org.springframework.data.domain.Pageable pageable);
}