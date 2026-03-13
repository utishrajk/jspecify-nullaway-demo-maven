package org.example.store;

import java.util.List;
import java.util.stream.Collectors;

public class BookStore {

    public record Book(String title, String author, String category, double price) {}

    public static List<Book> filterByCategory(List<Book> inventory, String category) {
        return inventory.stream()
            .filter(book -> category.equalsIgnoreCase(book.category()))
            .collect(Collectors.toList());
    }

    public static void main(String[] args) {
        List<Book> inventory = List.of(
            new Book("The Feynman Lectures on Physics", "Richard Feynman", "Physics", 150.0),
            new Book("Brief History of Time", "Stephen Hawking", "Physics", 20.0),
            new Book("Clean Code", "Robert C. Martin", "Software", 45.0),
            new Book("Quantum Mechanics: The Theoretical Minimum", "Leonard Susskind", "Physics", 35.0),
            new Book("The Art of Computer Programming", "Donald Knuth", "Software", 200.0),
            new Book("Introduction to Electrodynamics", "David Griffiths", "Physics", 80.0)
        );

        System.out.println("--- Books in Physics Category ---");
        List<Book> physicsBooks = filterByCategory(inventory, "Physics");

        physicsBooks.forEach(book -> 
            System.out.printf("Title: %-40s | Author: %-20s | Price: $%.2f%n", 
                book.title(), book.author(), book.price())
        );
    }
}
