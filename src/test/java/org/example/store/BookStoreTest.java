package org.example.store;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class BookStoreTest {

    @Test
    void shouldFilterPhysicsBooks() {
        List<BookStore.Book> inventory = List.of(
            new BookStore.Book("Physics 1", "Author A", "Physics", 10.0),
            new BookStore.Book("Software 1", "Author B", "Software", 20.0),
            new BookStore.Book("Physics 2", "Author C", "Physics", 30.0)
        );

        List<BookStore.Book> result = BookStore.filterByCategory(inventory, "Physics");

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(b -> "Physics".equals(b.category())));
        assertEquals("Physics 1", result.get(0).title());
        assertEquals("Physics 2", result.get(1).title());
    }

    @Test
    void shouldReturnEmptyListWhenNoCategoryMatches() {
        List<BookStore.Book> inventory = List.of(
            new BookStore.Book("Software 1", "Author B", "Software", 20.0)
        );

        List<BookStore.Book> result = BookStore.filterByCategory(inventory, "History");

        assertTrue(result.isEmpty());
    }
}
