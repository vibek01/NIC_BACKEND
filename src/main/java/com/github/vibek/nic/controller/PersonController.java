package com.github.vibek.nic.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.github.vibek.nic.dto.CaseDto.CaseResponseDTO;
import com.github.vibek.nic.dto.PersonDto.PersonRequestDTO;
import com.github.vibek.nic.dto.PersonDto.PersonResponseDTO;
import com.github.vibek.nic.dto.PersonDto.PushTokenRequestDTO;
import com.github.vibek.nic.entity.Person;
import com.github.vibek.nic.service.PersonService;

import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/persons")
@RequiredArgsConstructor
public class PersonController {

    private final PersonService personService;

    @PostMapping
    public ResponseEntity<PersonResponseDTO> createPerson(@RequestBody PersonRequestDTO personRequestDTO) {
        return ResponseEntity.ok(personService.createPerson(personRequestDTO));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PersonResponseDTO> getPersonById(@PathVariable UUID id) {
        return ResponseEntity.ok(personService.getPersonById(id));
    }

    @GetMapping
    public ResponseEntity<List<PersonResponseDTO>> getAllPersons() {
        return ResponseEntity.ok(personService.getAllPersons());
    }

    @PutMapping("/{id}")
    public ResponseEntity<PersonResponseDTO> updatePerson(@PathVariable UUID id, @RequestBody PersonRequestDTO personRequestDTO) {
        return ResponseEntity.ok(personService.updatePerson(id, personRequestDTO));
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<PersonResponseDTO>> createPersons(@RequestBody List<PersonRequestDTO> personRequestDTOList) {
        return ResponseEntity.ok(personService.createPersons(personRequestDTOList));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePerson(@PathVariable UUID id) {
        personService.deletePerson(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/login") // Note: This endpoint is not typically part of PersonController, but keeping as is.
    public ResponseEntity<String> login(@RequestParam String email, @RequestParam String password) {
        boolean isAuthenticated = personService.login(email, password);
        if (isAuthenticated) {
            return ResponseEntity.ok("Login successful");
        } else {
            return ResponseEntity.status(401).body("Invalid email or password");
        }
    }

    @PostMapping("/{id}/register-push-token")
    public ResponseEntity<Void> registerPushToken(@PathVariable UUID id, @Valid @RequestBody PushTokenRequestDTO requestDTO) {
        personService.registerPushToken(id, requestDTO.getPushToken());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/logout")
    public ResponseEntity<Void> logoutPerson(@PathVariable UUID id) {
        personService.clearPushToken(id);
        return ResponseEntity.ok().build();
    }

    // CORRECTED METHOD SIGNATURE
    @GetMapping("/search")
    public ResponseEntity<List<PersonResponseDTO>> searchPersons( // Return a list of DTOs
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) Integer rank,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) String designation,
            @RequestParam(required = false) String officeName,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String subdivision,
            @RequestParam(required = false) String postName
    ) {
        List<PersonResponseDTO> results = personService.search(role, department, rank, district, designation, officeName, status, subdivision, postName);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/email")
    public ResponseEntity<Person> getPersonByEmail(@RequestParam String email) {
        Person person = personService.getPersonByEmail(email);
        if (person != null) {
            return ResponseEntity.ok(person);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/name")
    public ResponseEntity<PersonResponseDTO> getPersonByName(
            @RequestParam String firstName,
            @RequestParam String lastName) {
        try {
            PersonResponseDTO person = personService.getPersonByName(firstName, lastName);
            return ResponseEntity.ok(person);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ====================== NEW ENDPOINT START ======================
    /**
     * Retrieves all cases assigned to a specific person. This is the new, efficient
     * endpoint that the frontend will call to populate the member dashboard.
     * @param id The UUID of the person.
     * @return A list of cases the person is assigned to.
     */
    @GetMapping("/{id}/cases")
    public ResponseEntity<List<CaseResponseDTO>> getAssignedCasesForPerson(@PathVariable UUID id) {
        List<CaseResponseDTO> cases = personService.getAssignedCasesForPerson(id);
        return ResponseEntity.ok(cases);
    }
    // ======================= NEW ENDPOINT END =======================
}