package com.github.vibek.nic.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.vibek.nic.entity.Departments;
import com.github.vibek.nic.repository.DepartmentRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class DepartmentService {
    @Autowired
    private DepartmentRepository departmentRepository;

    public List<Departments> getAllDepartments() {
        return departmentRepository.findAll();
    }

    public Optional<Departments> getDepartmentById(UUID id) {
        return departmentRepository.findById(id);
    }

    public Departments createDepartment(Departments department) {
        return departmentRepository.save(department);
    }

    public Departments updateDepartment(UUID id, Departments departmentDetails) {
        return departmentRepository.findById(id).map(department -> {
            department.setName(departmentDetails.getName());
            return departmentRepository.save(department);
        }).orElse(null);
    }

    public boolean deleteDepartment(UUID id) {
        return departmentRepository.findById(id).map(department -> {
            departmentRepository.delete(department);
            return true;
        }).orElse(false);
    }

    public Optional<Departments> getDepartmentByName(String name) {
        return departmentRepository.findByName(name);
    }
}
