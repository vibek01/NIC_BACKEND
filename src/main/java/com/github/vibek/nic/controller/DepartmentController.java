package com.github.vibek.nic.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.github.vibek.nic.entity.Departments;
import com.github.vibek.nic.service.DepartmentService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    @GetMapping
    public List<Departments> getAllDepartments() {  // Returns list of all departments
        return departmentService.getAllDepartments();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Departments> getDepartmentById(@PathVariable UUID id) {  // Fetch by UUID ID
        Optional<Departments> department = departmentService.getDepartmentById(id);
        return department.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public Departments createDepartment(@RequestBody Departments department) {  // Create new department
        return departmentService.createDepartment(department);
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<Departments> getDepartmentByName(@PathVariable String name) {  // Fetch by name string
        Optional<Departments> department = departmentService.getDepartmentByName(name);
        return department.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Departments> updateDepartment(@PathVariable UUID id, @RequestBody Departments departmentDetails) {  // Update by ID
        Departments updatedDepartment = departmentService.updateDepartment(id, departmentDetails);
        if (updatedDepartment == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updatedDepartment);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable UUID id) {  // Delete by ID
        boolean deleted = departmentService.deleteDepartment(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
