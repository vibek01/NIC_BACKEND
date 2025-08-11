// src/main/java/com/github/vibek/nic/repository/SubdivisionRepository.java
package com.github.vibek.nic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.github.vibek.nic.entity.Subdivision;

import java.util.Optional;

@Repository
public interface SubdivisionRepository extends JpaRepository<Subdivision, Long> {
    Optional<Subdivision> findByName(String name);
}