// src/main/java/com/github/vibek/nic/repository/PersonRepository.java
package com.github.vibek.nic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.github.vibek.nic.entity.Person;
import com.github.vibek.nic.entity.Subdivision;
import com.github.vibek.nic.enums.Role;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PersonRepository extends JpaRepository<Person, UUID> {
    Optional<Person> findByEmail(String email);

    // --- Methods using new Subdivision entity ---
    List<Person> findByRoleAndSubdivision(Role role, Subdivision subdivision);

    @Query("SELECT p FROM Person p WHERE p.department = :department AND p.subdivision = :subdivision AND p.role = :role AND p.rank >= :rank")
    List<Person> findByDepartmentAndSubdivisionAndRoleAndRankGreaterThanEqual(
            @Param("department") String department,
            @Param("subdivision") Subdivision subdivision,
            @Param("role") Role role,
            @Param("rank") Integer rank
    );

    @Query("SELECT p FROM Person p WHERE p.department = :department AND p.subdivision = :subdivision AND p.role = :role AND p.rank = :rank")
    List<Person> findByDepartmentAndSubdivisionAndRoleAndRank(
            @Param("department") String department,
            @Param("subdivision") Subdivision subdivision,
            @Param("role") Role role,
            @Param("rank") Integer rank
    );

    List<Person> findBySubdivision(Subdivision subdivision);

    @Query("SELECT p FROM Person p WHERE p.department = :department AND p.district.name = :districtName AND p.role = :role AND p.rank <= :rank")
    List<Person> findByDepartmentAndDistrictAndRoleAndRank(
            @Param("department") String department,
            @Param("districtName") String districtName,
            @Param("role") Role role,
            @Param("rank") Integer rank
    );

    @Query("SELECT p FROM Person p WHERE p.department = :department AND p.subdivision = :subdivision AND p.role = :role AND p.rank <= :rank")
    List<Person> findByDepartmentAndSubdivisionAndRoleAndRankLessThanEqual(
            @Param("department") String department,
            @Param("subdivision") Subdivision subdivision,
            @Param("role") Role role,
            @Param("rank") int rank
    );

    @Query("SELECT p FROM Person p WHERE p.role = :role AND p.district.name = :districtName")
    List<Person> findByRoleAndDistrict(
            @Param("role") Role role,
            @Param("districtName") String districtName
    );

    @Query("SELECT p FROM Person p WHERE p.role = :role AND p.district.name = :districtName AND p.subdivision = :subdivision")
    List<Person> findByRoleAndDistrictAndSubdivision(
            @Param("role") Role role,
            @Param("districtName") String districtName,
            @Param("subdivision") Subdivision subdivision
    );

    @Query("SELECT p FROM Person p WHERE p.department = :department AND p.district.name = :districtName AND p.subdivision = :subdivision AND p.role = :role AND p.rank = :rank")
    List<Person> findByDepartmentAndDistrictAndSubdivisionAndRoleAndRank(
            @Param("department") String department,
            @Param("districtName") String districtName,
            @Param("subdivision") Subdivision subdivision,
            @Param("role") Role role,
            @Param("rank") int rank
    );

    // =================================================================
    // CRITICAL METHOD for finding Team Leaders
    // =================================================================
    @Query("SELECT p FROM Person p WHERE p.role = :role AND p.subdivision = :subdivision AND p.rank = :rank")
    List<Person> findByRoleAndSubdivisionAndRank(
            @Param("role") Role role,
            @Param("subdivision") Subdivision subdivision,
            @Param("rank") int rank
    );

    @Query("SELECT DISTINCT p.department FROM Person p WHERE p.subdivision = :subdivision")
    List<String> findDistinctDepartmentsBySubdivision(@Param("subdivision") Subdivision subdivision);

    @Query("SELECT p FROM Person p WHERE p.department = :department AND p.district.name = :districtName AND p.role = :role AND p.rank >= :rank")
    List<Person> findByDepartmentAndDistrictAndRoleAndRankGreaterThanEqual(
            @Param("department") String department,
            @Param("districtName") String districtName,
            @Param("role") Role role,
            @Param("rank") int rank
    );

    @Query("SELECT p FROM Person p " +
        "LEFT JOIN FETCH p.district " +
        "LEFT JOIN FETCH p.subdivision " +
        "WHERE (:role IS NULL OR p.role = :role) " +
        "AND (:department IS NULL OR p.department = :department) " +
        "AND (:rank IS NULL OR p.rank = :rank) " +
        "AND (:districtName IS NULL OR p.district.name = :districtName) " +
        "AND (:designation IS NULL OR p.designation = :designation) " +
        "AND (:officeName IS NULL OR p.officeName = :officeName) " +
        "AND (:status IS NULL OR p.status = :status) " +
        "AND (:subdivisionName IS NULL OR p.subdivision.name = :subdivisionName) " +
        "AND (:postName IS NULL OR p.postName = :postName)")
    List<Person> findByFilters(
            @Param("role") Role role,
            @Param("department") String department,
            @Param("rank") Integer rank,
            @Param("districtName") String districtName,
            @Param("designation") String designation,
            @Param("officeName") String officeName,
            @Param("status") String status,
            @Param("subdivisionName") String subdivisionName,
            @Param("postName") String postName
    );

    Optional<Person> findByFirstNameAndLastName(String firstName, String lastName);
}