package com.github.vibek.nic.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.vibek.nic.entity.CaseDetails;

import java.util.UUID;

public interface CaseDetailsRepository extends JpaRepository<CaseDetails, UUID> {
}