// File: src/main/java/com/github/vibek/nic/repository/CaseRepository.java

package com.github.vibek.nic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.github.vibek.nic.entity.ChildMarriageCase;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CaseRepository extends JpaRepository<ChildMarriageCase, UUID> {

    // --- MODIFICATION START ---
    // This new query tells JPA to fetch the cases AND their associated CaseDetails list
    // all in a single database query, solving the lazy loading issue.
    @Query("SELECT DISTINCT c FROM ChildMarriageCase c LEFT JOIN FETCH c.caseDetails")
    List<ChildMarriageCase> findAllWithDetails();

    @Query("SELECT DISTINCT c FROM ChildMarriageCase c LEFT JOIN FETCH c.caseDetails WHERE c.id = :id")
    Optional<ChildMarriageCase> findByIdWithDetails(@Param("id") UUID id);
    // --- MODIFICATION END ---
}