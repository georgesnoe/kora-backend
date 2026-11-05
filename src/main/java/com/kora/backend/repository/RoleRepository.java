package com.kora.backend.repository;

import com.kora.backend.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, String> {

	Optional<Role> findByName(String name);

	boolean existsByName(String name);

	List<Role> findAllByNameIn(Collection<String> names);

	List<Role> findAllBySuperAdminOnlyTrue();

}
