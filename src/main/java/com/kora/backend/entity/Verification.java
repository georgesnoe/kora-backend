package com.kora.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "verification")
public class Verification extends BaseEntity {

	@Column(name = "identifier", nullable = false)
	private String identifier;

	@Column(name = "value", nullable = false)
	private String value;

	@Column(name = "expiresAt", nullable = false)
	private Instant expiresAt;

}
