package com.github.vibek.nic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.github.vibek.nic.entity.TeamFormation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TeamFormationRepository extends JpaRepository<TeamFormation, UUID> {
    List<TeamFormation> findByFormedAtIsNull();
    Optional<TeamFormation> findByCaseId_Id(UUID caseId);

    @Query("SELECT t FROM TeamFormation t WHERE t.departmentStatuses[:deptName] = :status")
    List<TeamFormation> findByDepartmentStatus(String deptName, String status);

    @Query("SELECT t FROM TeamFormation t JOIN t.departmentMembers m WHERE INDEX(m) = :departmentName")
    List<TeamFormation> findByDepartmentInvolvement(@Param("departmentName") String departmentName);

    // ====================== MODIFICATION START ======================
    // The method name is updated to query through the new 'members' relationship.
    // JPA automatically understands this nested property query.
    List<TeamFormation> findAllBySupervisor_IdOrMembers_Id(UUID supervisorId, UUID memberId);
    // ======================= MODIFICATION END =======================
}