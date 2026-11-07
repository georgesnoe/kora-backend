package com.kora.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "permissions")
public class Permission extends BaseEntity {

	@Column(name = "name", nullable = false, unique = true)
	private String name;

	@Column(name = "description")
	private String description;

	@Column(name = "superAdminOnly", nullable = false)
	private boolean superAdminOnly;

}
