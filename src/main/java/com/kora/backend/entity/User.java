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
@Table(name = "user")
public class User extends BaseEntity {

	@Column(name = "name", nullable = false)
	private String name;

	@Column(name = "email", nullable = false, unique = true)
	private String email;

	@Column(name = "emailVerified", nullable = false)
	private boolean emailVerified;

	@Column(name = "image")
	private String image;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name = "user_role", joinColumns = @JoinColumn(name = "userId", nullable = false), inverseJoinColumns = @JoinColumn(name = "roleId", nullable = false))
	private Set<Role> roles = new LinkedHashSet<>();

	public void addRole(Role role) {
		roles.add(role);
	}

	public void removeRole(Role role) {
		roles.remove(role);
	}

}
