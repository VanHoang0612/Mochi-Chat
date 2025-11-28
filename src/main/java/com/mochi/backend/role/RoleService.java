package com.mochi.backend.role;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    public Set<Role> getRolesByNames(List<String> roleNames) {
        return roleRepository.findByNameIn(roleNames);
    }

    public Optional<Role> findByName(String name) {
        return roleRepository.findByName(name);
    }
}
