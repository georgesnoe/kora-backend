package com.kora.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SourceType;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity {

	@Id
	@Column(name = "id", nullable = false, updatable = false)
	private String id;

	@Column(name = "createdAt", nullable = false, updatable = false)
	@CreationTimestamp(source = SourceType.DB)
	private Instant createdAt;

	@Column(name = "updatedAt", nullable = false)
	@UpdateTimestamp(source = SourceType.DB)
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

}
