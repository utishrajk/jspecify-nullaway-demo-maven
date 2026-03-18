package org.example.service;

import org.example.dto.BookResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {

    private final List<BookResponse> inventory = List.of(
        new BookResponse("The Feynman Lectures on Physics", "Richard Feynman", "Physics", 150.0),
        new BookResponse("Brief History of Time", "Stephen Hawking", "Physics", 20.0),
        new BookResponse("Clean Code", "Robert C. Martin", "Software", 45.0),
        new BookResponse("Quantum Mechanics: The Theoretical Minimum", "Leonard Susskind", "Physics", 35.0),
        new BookResponse("The Art of Computer Programming", "Donald Knuth", "Software", 200.0),
        new BookResponse("Introduction to Electrodynamics", "David Griffiths", "Physics", 80.0)
    );

    public List<BookResponse> getAllBooks() {
        return inventory;
    }

    public List<BookResponse> getBooksByCategory(String category) {
        return inventory.stream()
                .filter(book -> category.equalsIgnoreCase(book.category()))
                .toList();
    }
}
