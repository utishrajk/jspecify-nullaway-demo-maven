package org.example.service;

import org.example.dto.UserRequest;
import org.example.dto.UserResponse;
import org.example.entity.User;
import org.example.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class UserService {

    private final List<User> users = new ArrayList<>(List.of(
        new User(1L, "John", "Doe", "john.doe@example.com", 25, 50000.0),
        new User(2L, "Jane", "Smith", "jane.smith@example.com", 17, 45000.0),
        new User(3L, "Bob", "Johnson", "bob.johnson@example.com", 70, 60000.0),
        new User(4L, "Alice", "Brown", "alice.brown@example.com", 35, 55000.0)
    ));

    private final AtomicLong counter = new AtomicLong(5);

    public List<UserResponse> getAllUsers() {
        return users.stream()
                .map(UserMapper.INSTANCE::userToUserResponse)
                .toList();
    }

    public UserResponse addUser(UserRequest request) {
        User newUser = new User(
            counter.getAndIncrement(),
            request.getFirstName(),
            request.getLastName(),
            request.getEmail(),
            request.getAge(),
            request.getSalary()
        );
        users.add(newUser);
        return UserMapper.INSTANCE.userToUserResponse(newUser);
    }

    public UserResponse updateSalary(Long id, double newSalary) {
        User user = users.stream()
                .filter(u -> u.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setSalary(newSalary);
        return UserMapper.INSTANCE.userToUserResponse(user);
    }

    public UserResponse getUserById(Long id) {
        return users.stream()
                .filter(u -> u.getId().equals(id))
                .findFirst()
                .map(UserMapper.INSTANCE::safeUserMapping)
                .orElseGet(() -> getUserByIdSafely(id));
    }

    public UserResponse getUserByIdSafely(Long id) {
        User userFromDb = users.stream()
                .filter(u -> u.getId().equals(id))
                .findFirst()
                .orElse(null);
        
        UserResponse response = UserMapper.INSTANCE.safeUserMapping(userFromDb);
        if (response == null) {
            response = new UserResponse();
            response.setId(id);
            response.setFullName("User Not Found");
            response.setEmail("N/A");
            response.setAgeGroup(UserMapper.INSTANCE.safeStringConversion(null));
        }
        return response;
    }
}
