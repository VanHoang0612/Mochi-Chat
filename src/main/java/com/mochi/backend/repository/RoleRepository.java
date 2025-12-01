package com.mochi.backend.repository;

import com.mochi.backend.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {
    Set<Role> findByNameIn(List<String> roleNames);

    Optional<Role> findByName(String name);
}
