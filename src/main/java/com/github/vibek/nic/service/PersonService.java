// src/main/java/com/github/vibek/nic/service/PersonService.java
package com.github.vibek.nic.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.vibek.nic.dto.CaseDto.CaseResponseDTO;
import com.github.vibek.nic.dto.PersonDto.PersonRequestDTO;
import com.github.vibek.nic.dto.PersonDto.PersonResponseDTO;
import com.github.vibek.nic.entity.*;
import com.github.vibek.nic.enums.Role;
import com.github.vibek.nic.repository.*;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PersonService {

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private TeamFormationRepository teamFormationRepository;

    @Autowired
    @Lazy
    private CaseService caseService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // ADDED: Repositories for looking up District and Subdivision
    @Autowired
    private DistrictRepository districtRepository;

    @Autowired
    private SubdivisionRepository subdivisionRepository;

    @Transactional
    public PersonResponseDTO createPerson(PersonRequestDTO personRequestDTO) {
        Person person = new Person();
        person.setFirstName(personRequestDTO.getFirstName());
        person.setLastName(personRequestDTO.getLastName());
        person.setEmail(personRequestDTO.getEmail());
        person.setPhoneNumber(personRequestDTO.getPhoneNumber());
        person.setGender(personRequestDTO.getGender());
        person.setAddress(personRequestDTO.getAddress());
        person.setRole(Role.valueOf(personRequestDTO.getRole()));

        departmentRepository.findByName(personRequestDTO.getDepartment())
                .orElseThrow(() -> new RuntimeException("Department not found: " + personRequestDTO.getDepartment()));
        person.setDepartment(personRequestDTO.getDepartment());

        Post post = postRepository.findByPostNameAndDepartment(personRequestDTO.getDesignation(), personRequestDTO.getDepartment())
                .orElseThrow(() -> new RuntimeException("Post not found for designation: " + personRequestDTO.getDesignation() + " in department: " + personRequestDTO.getDepartment()));
        person.setDesignation(personRequestDTO.getDesignation());
        person.setRank(post.getRank());
        person.setPostName(post.getPostName());

        // MODIFIED: Look up and set District and Subdivision entities
        if (personRequestDTO.getDistrict() != null && !personRequestDTO.getDistrict().isEmpty()) {
            District district = districtRepository.findByName(personRequestDTO.getDistrict())
                    .orElseThrow(() -> new RuntimeException("District not found: " + personRequestDTO.getDistrict()));
            person.setDistrict(district);
        }

        if (personRequestDTO.getSubdivision() != null && !personRequestDTO.getSubdivision().isEmpty()) {
            Subdivision subdivision = subdivisionRepository.findByName(personRequestDTO.getSubdivision())
                    .orElseThrow(() -> new RuntimeException("Subdivision not found: " + personRequestDTO.getSubdivision()));
            person.setSubdivision(subdivision);
        }

        person.setOfficeName(personRequestDTO.getOfficeName());
        person.setStatus(personRequestDTO.getStatus());

        person.setPassword(passwordEncoder.encode(personRequestDTO.getPassword()));
        Person savedPerson = personRepository.save(person);
        return mapToResponseDTO(savedPerson);
    }

    public List<PersonResponseDTO> createPersons(List<PersonRequestDTO> personRequestDTOList) {
        List<Person> persons = personRequestDTOList.stream().map(dto -> {
            Person person = new Person();
            person.setFirstName(dto.getFirstName());
            person.setLastName(dto.getLastName());
            person.setEmail(dto.getEmail());
            person.setPhoneNumber(dto.getPhoneNumber());
            person.setGender(dto.getGender());
            person.setAddress(dto.getAddress());
            if (dto.getRole() != null) {
                person.setRole(Role.valueOf(dto.getRole()));
            } else {
                person.setRole(null);
            }

            departmentRepository.findByName(dto.getDepartment())
                    .orElseThrow(() -> new RuntimeException("Department not found: " + dto.getDepartment()));
            person.setDepartment(dto.getDepartment());

            person.setDesignation(dto.getDesignation());
            person.setPostName(dto.getPostName());
            person.setRank(dto.getRank());

            if (dto.getDistrict() != null && !dto.getDistrict().isEmpty()) {
                District district = districtRepository.findByName(dto.getDistrict())
                        .orElseThrow(() -> new RuntimeException("District not found: " + dto.getDistrict()));
                person.setDistrict(district);
            }

            if (dto.getSubdivision() != null && !dto.getSubdivision().isEmpty()) {
                Subdivision subdivision = subdivisionRepository.findByName(dto.getSubdivision())
                        .orElseThrow(() -> new RuntimeException("Subdivision not found: " + dto.getSubdivision()));
                person.setSubdivision(subdivision);
            }

            person.setOfficeName(dto.getOfficeName());
            person.setStatus(dto.getStatus());

            person.setPassword(passwordEncoder.encode(dto.getPassword()));
            return person;
        }).toList();

        List<Person> savedPersons = personRepository.saveAll(persons);
        return savedPersons.stream().map(this::mapToResponseDTO).toList();
    }

    @Transactional
    public void registerPushToken(UUID personId, String pushToken) {
        Person person = personRepository.findById(personId)
                .orElseThrow(() -> new RuntimeException("Person not found with ID: " + personId));
        person.setPushToken(pushToken);
        personRepository.save(person);
    }

    public PersonResponseDTO getPersonById(UUID id) {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Person not found with ID: " + id));
        return mapToResponseDTO(person);
    }

    public List<PersonResponseDTO> getAllPersons() {
        return personRepository.findAll().stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public PersonResponseDTO updatePerson(UUID id, PersonRequestDTO personRequestDTO) {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Person not found with ID: " + id));
        person.setFirstName(personRequestDTO.getFirstName());
        person.setLastName(personRequestDTO.getLastName());
        person.setEmail(personRequestDTO.getEmail());
        person.setPhoneNumber(personRequestDTO.getPhoneNumber());
        person.setGender(personRequestDTO.getGender());
        person.setAddress(personRequestDTO.getAddress());
        person.setRole(Role.valueOf(personRequestDTO.getRole()));

        if (personRequestDTO.getDepartment() != null) {
            departmentRepository.findByName(personRequestDTO.getDepartment())
                    .orElseThrow(() -> new RuntimeException("Department not found: " + personRequestDTO.getDepartment()));
            person.setDepartment(personRequestDTO.getDepartment());
        }

        if (personRequestDTO.getDesignation() != null) {
            String deptName = personRequestDTO.getDepartment() != null ? personRequestDTO.getDepartment() : person.getDepartment();
            if (deptName == null) {
                throw new RuntimeException("Department required for designation update");
            }
            Post post = postRepository.findByPostNameAndDepartment(personRequestDTO.getDesignation(), deptName)
                    .orElseThrow(() -> new RuntimeException("Post not found"));
            person.setDesignation(personRequestDTO.getDesignation());
            person.setRank(post.getRank());
            person.setPostName(post.getPostName());
        }

        // MODIFIED: Update logic for District and Subdivision
        if (personRequestDTO.getDistrict() != null && !personRequestDTO.getDistrict().isEmpty()) {
            District district = districtRepository.findByName(personRequestDTO.getDistrict())
                    .orElseThrow(() -> new RuntimeException("District not found: " + personRequestDTO.getDistrict()));
            person.setDistrict(district);
        } else {
            person.setDistrict(null);
        }

        if (personRequestDTO.getSubdivision() != null && !personRequestDTO.getSubdivision().isEmpty()) {
            Subdivision subdivision = subdivisionRepository.findByName(personRequestDTO.getSubdivision())
                    .orElseThrow(() -> new RuntimeException("Subdivision not found: " + personRequestDTO.getSubdivision()));
            person.setSubdivision(subdivision);
        } else {
            person.setSubdivision(null);
        }

        if (personRequestDTO.getPassword() != null && !personRequestDTO.getPassword().isEmpty()) {
            person.setPassword(passwordEncoder.encode(personRequestDTO.getPassword()));
        }

        Person updatedPerson = personRepository.save(person);
        return mapToResponseDTO(updatedPerson);
    }

    public void deletePerson(UUID id) {
        personRepository.deleteById(id);
    }

    public boolean login(String email, String password) {
        Person person = personRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Person not found with email: " + email));
        return passwordEncoder.matches(password, person.getPassword());
    }

    public List<PersonResponseDTO> search(String role, String department, Integer rank, String district,
                                       String designation, String officeName, String status,
                                       String subdivision, String postName) {
        Role roleEnum = null;
        if (role != null && !role.trim().isEmpty()) {
            try {
                roleEnum = Role.valueOf(role.toUpperCase().trim());
            } catch (IllegalArgumentException e) {
                return Collections.emptyList();
            }
        }
        // Fetch the entities from the repository
        List<Person> persons = personRepository.findByFilters(
                roleEnum, department, rank, district, designation,
                officeName, status, subdivision, postName
        );
        // Map the entities to DTOs before returning
        return persons.stream().map(this::mapToResponseDTO).collect(Collectors.toList());
    }

    public Person getPersonByEmail(String email) {
        return personRepository.findByEmail(email).orElse(null);
    }

    public PersonResponseDTO getPersonByName(String firstName, String lastName) {
        Person person = personRepository.findByFirstNameAndLastName(firstName, lastName)
                .orElseThrow(() -> new RuntimeException("Person not found with name: " + firstName + " " + lastName));
        return mapToResponseDTO(person);
    }

    @Transactional
    public void clearPushToken(UUID personId) {
        personRepository.findById(personId).ifPresent(person -> {
            person.setPushToken(null);
            personRepository.save(person);
            System.out.println("Cleared push token for user: " + personId);
        });
    }

    public List<CaseResponseDTO> getAssignedCasesForPerson(UUID personId) {
        List<TeamFormation> teams = teamFormationRepository.findAllBySupervisor_IdOrMembers_Id(personId, personId);

        if (teams.isEmpty()) {
            return Collections.emptyList();
        }

        List<ChildMarriageCase> cases = teams.stream()
                .map(TeamFormation::getCaseId)
                .distinct()
                .collect(Collectors.toList());

        return cases.stream()
                .map(caseEntity -> caseService.mapToResponseDTO(caseEntity))
                .collect(Collectors.toList());
    }

    private PersonResponseDTO mapToResponseDTO(Person person) {
        PersonResponseDTO dto = new PersonResponseDTO();
        dto.setId(person.getId());
        dto.setFirstName(person.getFirstName());
        dto.setLastName(person.getLastName());
        dto.setEmail(person.getEmail());
        dto.setPhoneNumber(person.getPhoneNumber());
        dto.setGender(person.getGender());
        dto.setAddress(person.getAddress());

        if (person.getRole() != null) {
            dto.setRole(person.getRole().name());
        } else {
            dto.setRole(null);
        }

        dto.setDepartment(person.getDepartment());
        dto.setCreatedAt(person.getCreatedAt());
        dto.setUpdatedAt(person.getUpdatedAt());

        // MODIFIED: Map entity objects back to String names for the DTO
        if (person.getDistrict() != null) {
            dto.setDistrict(person.getDistrict().getName());
        }
        if (person.getSubdivision() != null) {
            dto.setSubdivision(person.getSubdivision().getName());
        }

        dto.setDesignation(person.getDesignation());
        dto.setOfficeName(person.getOfficeName());
        dto.setStatus(person.getStatus());
        dto.setRank(person.getRank());
        dto.setPostName(person.getPostName());
        
        return dto;
    }
}