package com.ulms.service;

import com.ulms.dto.request.BookRequest;
import com.ulms.model.Book;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BookService {
    List<Book> getAllBooks(String query, String category, Pageable pageable);
    Book getBookById(Long id);
    Book createBook(BookRequest request, Long addedById);
    Book updateBook(Long id, BookRequest request, Long updatedById);
    void deleteBook(Long id, Long deletedById);
    List<Book> bulkImport(List<BookRequest> books, Long importedById);
}