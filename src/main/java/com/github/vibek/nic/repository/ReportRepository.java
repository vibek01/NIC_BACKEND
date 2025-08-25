// src/main/java/com/github/vibek/nic/repository/ReportRepository.java
package com.github.vibek.nic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.github.vibek.nic.entity.Report;
import com.github.vibek.nic.enums.ReportStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    List<Report> findByDepartmentOrderBySubmittedAtDesc(String department);

    List<Report> findByCaseEntity_IdAndIsFinalReportFalse(UUID caseId);
    Optional<Report> findByCaseEntity_IdAndIsFinalReportTrue(UUID caseId);
    List<Report> findByCaseEntity_Id(UUID caseId);
    List<Report> findByPersonId(UUID personId);
    boolean existsByCaseEntity_IdAndPersonIdAndIsFinalReportFalse(UUID caseId, UUID personId);
    boolean existsByCaseEntity_IdAndDepartmentAndIsFinalReportFalse(UUID caseId, String department);

    // =================================================================
    // MODIFIED: The query now joins to the new entities and checks the 'name' property.
    // =================================================================
    @Query("SELECT r FROM Report r LEFT JOIN r.caseEntity.caseDetails cd WHERE " +
           "(:boySubdivision IS NULL OR cd.boySubdivision.name = :boySubdivision) " +
           "AND (:girlSubdivision IS NULL OR cd.girlSubdivision.name = :girlSubdivision) " +
           "AND (:marriageAddress IS NULL OR cd.marriageAddress LIKE %:marriageAddress%) " +
           "AND (:policeStation IS NULL OR cd.policeStationNearMarriageLocation = :policeStation) " +
           "AND (:year IS NULL OR EXTRACT(YEAR FROM r.submittedAt) = :year) " +
           "AND (:month IS NULL OR EXTRACT(MONTH FROM r.submittedAt) = :month) " +
           "AND (:department IS NULL OR r.department = :department) " +
           "AND (:status IS NULL OR r.status = :status) " +
           "AND (:district IS NULL OR r.personId IN (SELECT p.id FROM Person p WHERE p.district.name = :district)) " +
           "AND r.isFinalReport = false")
    List<Report> findByMultipleFilters(
        @Param("boySubdivision") String boySubdivision,
        @Param("girlSubdivision") String girlSubdivision,
        @Param("marriageAddress") String marriageAddress,
        @Param("policeStation") String policeStation,
        @Param("year") Integer year,
        @Param("month") Integer month,
        @Param("department") String department,
        @Param("status") ReportStatus status,
        @Param("district") String district
    );

    // ====================== NEW METHOD FOR DM DASHBOARD ======================
    @Query("SELECT r FROM Report r JOIN r.caseEntity ce JOIN ce.caseDetails cd " +
           "WHERE r.isFinalReport = true AND cd.girlSubdivision.district.name = :districtName")
    List<Report> findFinalReportsByDistrictName(@Param("districtName") String districtName);
}