package com.kora.backend.repository;

import com.kora.backend.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PermissionRepository extends JpaRepository<Permission, String> {

	Optional<Permission> findByName(String name);

	boolean existsByName(String name);

	List<Permission> findAllByNameIn(Collection<String> names);

	List<Permission> findAllBySuperAdminOnlyTrue();

}
