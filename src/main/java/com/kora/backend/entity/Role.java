package com.kora.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "roles")
public class Role extends BaseEntity {

	@Column(name = "name", nullable = false, unique = true)
	private String name;

	@Column(name = "description")
	private String description;

	@Column(name = "superAdminOnly", nullable = false)
	private boolean superAdminOnly;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "roles_permissions", joinColumns = @JoinColumn(name = "roleId", nullable = false), inverseJoinColumns = @JoinColumn(name = "permissionId", nullable = false))
	private Set<Permission> permissions = new LinkedHashSet<>();

	public void addPermission(Permission permission) {
		permissions.add(permission);
	}

	public void removePermission(Permission permission) {
		permissions.remove(permission);
	}

}
