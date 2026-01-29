package com.example.Controller;

import com.example.dto.CreateUserRequest;
import com.example.entities.Users;
import com.example.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }
//    create a user
    @PostMapping
    public ResponseEntity<Users>createUser(@Valid @RequestBody CreateUserRequest request){
        Users user = userService.createUser(request);
        return ResponseEntity.status(201).body(user);
    }
    // Get user by ID
    @GetMapping("/{userId}")
    public ResponseEntity<Users> getUser(@PathVariable Long userId) {
        return userService.getUserById(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Get all users
    @GetMapping
    public ResponseEntity<Iterable<Users>> getAllUsers() {
        Iterable<Users> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
}
