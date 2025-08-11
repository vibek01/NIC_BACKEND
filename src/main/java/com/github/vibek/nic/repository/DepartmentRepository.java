package com.github.vibek.nic.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.vibek.nic.entity.Departments;

import java.util.Optional;
import java.util.UUID;

public interface DepartmentRepository extends JpaRepository<Departments, UUID> {
    Optional<Departments> findByName(String name);

    boolean existsByName(String dept);
}
