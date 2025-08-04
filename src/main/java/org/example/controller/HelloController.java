package org.example.controller;

import org.example.dto.UserResponse;
import org.example.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class HelloController {

    private final UserService userService;

    public HelloController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/hello")
    public List<UserResponse> hello() {
        return userService.getAllUsers();
    }

    @GetMapping("/hello/{id}")
    public UserResponse helloUser(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @GetMapping("/hello/safe/{id}")
    public UserResponse helloUserSafely(@PathVariable Long id) {
        return userService.getUserByIdSafely(id);
    }
}