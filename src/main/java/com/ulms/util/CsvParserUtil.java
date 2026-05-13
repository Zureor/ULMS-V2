package com.ulms.util;

import com.ulms.dto.request.BookRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CsvParserUtil {

    private static final Logger log = LoggerFactory.getLogger(CsvParserUtil.class);

    public static List<BookRequest> parseCsv(MultipartFile file) throws IOException {
        List<BookRequest> books = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            String line;
            boolean isFirstRow = true;
            while ((line = reader.readLine()) != null) {
                if (isFirstRow) {
                    isFirstRow = false;
                    continue;
                }
                String[] fields = line.split(",");
                if (fields.length >= 5) {
                    BookRequest book = new BookRequest();
                    book.setIsbn(fields[0].trim());
                    book.setTitle(fields[1].trim());
                    book.setAuthor(fields[2].trim());
                    book.setPublisher(fields.length > 3 ? fields[3].trim() : "");
                    book.setCategory(fields.length > 4 ? fields[4].trim() : "General");
                    book.setTotalCopies(1);
                    books.add(book);
                }
            }
        }
        return books;
    }
}