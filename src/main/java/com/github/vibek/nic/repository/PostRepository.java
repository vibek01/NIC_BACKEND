package com.github.vibek.nic.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.github.vibek.nic.entity.Post;
import com.github.vibek.nic.enums.Department;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {

    List<Post> findByDepartmentAndRankLessThan(Department department, Integer rank);
    Optional<Post> findByPostNameAndDepartment(String postName, String department);
}