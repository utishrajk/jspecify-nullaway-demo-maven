package org.example.controller;

import org.example.dto.BookResponse;
import org.example.dto.UserRequest;
import org.example.dto.UserResponse;
import org.example.service.BookService;
import org.example.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class HelloController {

    private static final Logger logger = LoggerFactory.getLogger(HelloController.class);

    private final UserService userService;
    private final BookService bookService;

    public HelloController(UserService userService, BookService bookService) {
        this.userService = userService;
        this.bookService = bookService;
    }

    @GetMapping("/hello")
    public List<UserResponse> hello() {
        logger.info("Endpoint /hello called");
        return userService.getAllUsers();
    }

    @GetMapping("/books")
    public List<BookResponse> getBooks(@RequestParam(required = false) String category) {
        logger.info("Endpoint /books called with category: {}", category);
        if (category != null) {
            return bookService.getBooksByCategory(category);
        }
        return bookService.getAllBooks();
    }

    @PostMapping("/hello")
    public UserResponse addUser(@RequestBody UserRequest request) {
        logger.info("Endpoint POST /hello called for user: {} {}", request.getFirstName(), request.getLastName());
        return userService.addUser(request);
    }

    @PatchMapping("/hello/{id}/salary")
    public UserResponse updateSalary(@PathVariable Long id, @RequestParam double salary) {
        logger.info("Endpoint PATCH /hello/{}/salary called with salary: {}", id, salary);
        return userService.updateSalary(id, salary);
    }

    @GetMapping("/hello/{id}")
    public UserResponse helloUser(@PathVariable Long id) {
        logger.info("Endpoint /hello/{} called", id);
        return userService.getUserById(id);
    }

    @GetMapping("/hello/safe/{id}")
    public UserResponse helloUserSafely(@PathVariable Long id) {
        logger.info("Endpoint /hello/safe/{} called", id);
        return userService.getUserByIdSafely(id);
    }
}