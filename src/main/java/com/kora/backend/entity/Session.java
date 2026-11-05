package com.kora.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "session")
public class Session extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "userId", nullable = false)
	private User user;

	@Column(name = "token", nullable = false, unique = true)
	private String token;

	@Column(name = "expiresAt", nullable = false)
	private Instant expiresAt;

	@Column(name = "ipAddress")
	private String ipAddress;

	@Column(name = "userAgent")
	private String userAgent;

}
