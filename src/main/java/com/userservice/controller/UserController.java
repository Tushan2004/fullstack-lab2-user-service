package com.userservice.controller;

import com.userservice.dto.LoginRequest;
import com.userservice.dto.RegisterRequest;
import com.userservice.dto.RegisterResponse;
import com.userservice.entity.User;
import com.userservice.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3001"})
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepo;

    public UserController(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @PostMapping("/register")
    @Transactional
    public ResponseEntity<?> register(@RequestBody RegisterRequest r) {

        // Basic validation
        if (r.role() == null)
            return ResponseEntity.badRequest().body("Role is required");
        if (r.email() == null || r.email().isBlank())
            return ResponseEntity.badRequest().body("Email is required");
        if (r.password() == null || r.password().isBlank())
            return ResponseEntity.badRequest().body("Password is required");

        // Check duplicate email
        if (userRepo.findByEmail(r.email()).isPresent()) {
            return ResponseEntity.badRequest().body("Email already in use");
        }

        // 1) Create User
        var u = new User();
        u.setEmail(r.email());
        u.setRole(r.role());
        u.setPassword(r.password());
        userRepo.save(u);

        return ResponseEntity.ok(
                Map.of(
                        "id", u.getId(),
                        "role", u.getRole()  // or u.getRole().name()
                )
        );
    }

    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody LoginRequest req) {
        var userOpt = userRepo.findByEmail(req.email());

        // user not found
        if (userOpt.isEmpty()) {
            // 401 with NO body (type is still ResponseEntity<User>)
            return ResponseEntity.status(401).build();
        }

        var user = userOpt.get();

        // wrong password
        if (!req.password().equals(user.getPassword())) {
            return ResponseEntity.status(401).build();
        }

        // success -> return the User entity
        return ResponseEntity.ok(user);
    }
}
