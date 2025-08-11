package com.github.vibek.nic.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.github.vibek.nic.dto.LoginDto.LoginRequest;
import com.github.vibek.nic.dto.LoginDto.LoginResponse;
import com.github.vibek.nic.entity.Person;
import com.github.vibek.nic.repository.PersonRepository;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private PersonRepository personRepository;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest loginRequest) {
        Optional<Person> optionalPerson = personRepository.findByEmail(loginRequest.getEmail());

        if (optionalPerson.isEmpty()) {
            return new LoginResponse("Invalid email or password", null);
        }

        Person person = optionalPerson.get();

        // Assuming password is already hashed using BCrypt
        if (!org.springframework.security.crypto.bcrypt.BCrypt.checkpw(loginRequest.getPassword(), person.getPassword())) {
            return new LoginResponse("Invalid email or password", null);
        }

        // You can create a dummy token or just return user info (simplified approach)
        return new LoginResponse("Login successful", person.getId());
    }

    @PostMapping("/logout")
    public String logout() {
        // In stateless API, logout is frontend-only (clear local storage / token)
        return "Logged out successfully";
    }
}
