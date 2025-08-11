// src/main/java/com/github/vibek/nic/repository/WithdrawalRequestRepository.java
package com.github.vibek.nic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.github.vibek.nic.entity.WithdrawalRequest;
import com.github.vibek.nic.enums.WithdrawalRequestStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WithdrawalRequestRepository extends JpaRepository<WithdrawalRequest, UUID> {

    /**
     * Finds all pending withdrawal requests for a specific department and subdivision.
     * This is used by supervisors to populate their dashboard view.
     */
    // =================================================================
    // MODIFIED: The query now joins to the requester's subdivision and checks the 'name'.
    // =================================================================
    @Query("SELECT wr FROM WithdrawalRequest wr WHERE wr.status = :status AND wr.requester.department = :department AND wr.requester.subdivision.name = :subdivision")
    List<WithdrawalRequest> findByStatusAndDepartmentAndSubdivision(
        @Param("status") WithdrawalRequestStatus status,
        @Param("department") String department,
        @Param("subdivision") String subdivision
    );

    /**
     * Finds a specific withdrawal request made by a particular person for a particular case.
     * This is used by a team member to check if they have an outstanding request.
     */
    Optional<WithdrawalRequest> findByCaseEntity_IdAndRequester_Id(UUID caseId, UUID requesterId);
}