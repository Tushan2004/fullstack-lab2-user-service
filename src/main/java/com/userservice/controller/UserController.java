package com.userservice.controller;

import com.userservice.dto.LoginRequest;
import com.userservice.dto.LoginResponse;
import com.userservice.dto.RegisterRequest;
import com.userservice.dto.RegisterResponse;
import com.userservice.entity.User;
import com.userservice.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

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

        Long patientId = null, practitionerId = null;

        // 2) Create domain profile
        switch (r.role()) {
            case PATIENT -> {
                var p = new Patient(r.firstName(), r.lastName());
                p.setUser(u);
                patientRepo.save(p);
                patientId = p.getId();
            }
            case DOCTOR, STAFF -> {
                var pr = new Practitioner();
                pr.setFirstName(r.firstName());
                pr.setLastName(r.lastName());
                pr.setRole(r.role());
                pr.setUser(u);
                practitionerRepo.save(pr);
                practitionerId = pr.getId();
            }
        }

        return ResponseEntity.ok(new RegisterResponse(
                u.getId(),
                patientId,
                practitionerId,
                r.role()
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        var userOpt = userRepo.findByEmail(req.email());
        if (userOpt.isEmpty())
            return ResponseEntity.status(401).body("Invalid credentials");

        var user = userOpt.get();

        // PLAIN TEXT compare (no hashing)
        if (!req.password().equals(user.getPassword()))
            return ResponseEntity.status(401).body("Invalid credentials");

        return ResponseEntity.ok(new LoginResponse(
                user.getId(),
                user.getEmail(),
                user.getRole()
        ));
    }
}
