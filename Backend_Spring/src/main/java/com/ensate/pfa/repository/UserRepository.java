package com.ensate.pfa.repository;

import com.ensate.pfa.entity.User;
import com.ensate.pfa.entity.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    List<User> findByRole(Role role);
    List<User> findByDepartmentDepartmentId(Long departmentId);
    boolean existsByEmail(String email);
}
