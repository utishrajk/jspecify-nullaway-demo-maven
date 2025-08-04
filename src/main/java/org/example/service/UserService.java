package org.example.service;

import org.example.dto.UserResponse;
import org.example.entity.User;
import org.example.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class UserService {

    public List<UserResponse> getAllUsers() {
        List<User> usersFromDb = Arrays.asList(
            new User(1L, "John", "Doe", "john.doe@example.com", 25),
            new User(2L, "Jane", "Smith", "jane.smith@example.com", 17),
            new User(3L, "Bob", "Johnson", "bob.johnson@example.com", 70),
            new User(4L, "Alice", "Brown", "alice.brown@example.com", 35)
        );

        return usersFromDb.stream()
                .map(UserMapper.INSTANCE::userToUserResponse)
                .toList();
    }

    public UserResponse getUserById(Long id) {
        User userFromDb = new User(id, "Demo", "User", "demo.user@example.com", 30);
        
        return UserMapper.INSTANCE.safeUserMapping(userFromDb);
    }

    public UserResponse getUserByIdSafely(Long id) {
        User userFromDb = id > 100 ? null : new User(id, "Safe", "User", "safe.user@example.com", 25);
        
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