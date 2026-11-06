package com.kora.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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
