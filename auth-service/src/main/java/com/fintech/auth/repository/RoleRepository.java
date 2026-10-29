package com.fintech.auth.repository;

import com.fintech.auth.entity.Role;
import com.fintech.auth.entity.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName name);
}