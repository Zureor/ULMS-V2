package com.ulms.service.impl;

import com.ulms.dto.request.BookRequest;
import com.ulms.model.AuditLog;
import com.ulms.model.Book;
import com.ulms.model.User;
import com.ulms.repository.AuditLogRepository;
import com.ulms.repository.BookRepository;
import com.ulms.repository.UserRepository;
import com.ulms.service.BookService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BookServiceImpl implements BookService {

    private static final Logger log = LoggerFactory.getLogger(BookServiceImpl.class);

    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;

    public BookServiceImpl(BookRepository bookRepository,
                           UserRepository userRepository,
                           AuditLogRepository auditLogRepository) {
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public List<Book> getAllBooks(String query, String category, Pageable pageable) {
        return bookRepository.searchBooks(query, category, pageable);
    }

    @Override
    public Book getBookById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + id));
    }

    @Override
    @Transactional
    public Book createBook(BookRequest request, Long addedById) {
        if (bookRepository.existsByIsbn(request.getIsbn())) {
            throw new RuntimeException("Book with ISBN " + request.getIsbn() + " already exists");
        }
        User addedBy = userRepository.findById(addedById)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Book book = new Book();
        book.setIsbn(request.getIsbn());
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setPublisher(request.getPublisher());
        book.setEdition(request.getEdition());
        book.setCategory(request.getCategory());
        book.setTotalCopies(request.getTotalCopies());
        book.setAvailableCopies(request.getTotalCopies());
        book.setDescription(request.getDescription());
        book.setCoverImageUrl(request.getCoverImageUrl());
        book.setAddedBy(addedBy);
        Book saved = bookRepository.save(book);
        audit(addedBy, "CREATE", "Book", saved.getId(), "Created book: " + saved.getTitle());
        return saved;
    }

    @Override
    @Transactional
    public Book updateBook(Long id, BookRequest request, Long updatedById) {
        Book existing = getBookById(id);
        existing.setIsbn(request.getIsbn());
        existing.setTitle(request.getTitle());
        existing.setAuthor(request.getAuthor());
        existing.setPublisher(request.getPublisher());
        existing.setEdition(request.getEdition());
        existing.setCategory(request.getCategory());
        existing.setDescription(request.getDescription());
        existing.setCoverImageUrl(request.getCoverImageUrl());
        int oldTotal = existing.getTotalCopies();
        int newTotal = request.getTotalCopies();
        int diff = newTotal - oldTotal;
        existing.setTotalCopies(newTotal);
        existing.setAvailableCopies(existing.getAvailableCopies() + diff);
        User updatedBy = userRepository.findById(updatedById).orElse(null);
        if (updatedBy != null) {
            audit(updatedBy, "UPDATE", "Book", id, "Updated book: " + existing.getTitle());
        }
        return bookRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteBook(Long id, Long deletedById) {
        Book book = getBookById(id);
        User deletedBy = userRepository.findById(deletedById).orElse(null);
        audit(deletedBy, "DELETE", "Book", id, "Deleted book: " + book.getTitle());
        bookRepository.deleteById(id);
    }

    @Override
    @Transactional
    public List<Book> bulkImport(List<BookRequest> books, Long importedById) {
        User importedBy = userRepository.findById(importedById)
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<Book> saved = new ArrayList<>();
        for (BookRequest req : books) {
            if (bookRepository.existsByIsbn(req.getIsbn())) {
                log.warn("Skipping duplicate ISBN: {}", req.getIsbn());
                continue;
            }
            Book book = new Book();
            book.setIsbn(req.getIsbn());
            book.setTitle(req.getTitle());
            book.setAuthor(req.getAuthor());
            book.setPublisher(req.getPublisher());
            book.setEdition(req.getEdition());
            book.setCategory(req.getCategory());
            book.setTotalCopies(req.getTotalCopies());
            book.setAvailableCopies(req.getTotalCopies());
            book.setDescription(req.getDescription());
            book.setCoverImageUrl(req.getCoverImageUrl());
            book.setAddedBy(importedBy);
            saved.add(bookRepository.save(book));
        }
        audit(importedBy, "BULK_IMPORT", "Book", null, "Imported " + saved.size() + " books");
        return saved;
    }

    private void audit(User user, String action, String entityType, Long entityId, String details) {
        if (user == null) return;
        AuditLog logEntry = new AuditLog();
        logEntry.setAction(action);
        logEntry.setEntityType(entityType);
        logEntry.setEntityId(entityId);
        logEntry.setPerformedBy(user);
        logEntry.setDetails(details);
        auditLogRepository.save(logEntry);
    }
}