package com.kora.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity {

	@Id
	@Column(name = "id", nullable = false, updatable = false)
	private String id;

	@Column(name = "createdAt", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updatedAt", nullable = false)
	private Instant updatedAt;

	@PrePersist
	protected void onCreate() {
		Instant now = Instant.now();

		if (id == null || id.isBlank()) {
			id = UUID.randomUUID().toString();
		}

		if (createdAt == null) {
			createdAt = now;
		}

		if (updatedAt == null) {
			updatedAt = now;
		}
	}

	@PreUpdate
	protected void onUpdate() {
		updatedAt = Instant.now();
	}

}
