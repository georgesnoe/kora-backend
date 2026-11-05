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
@Table(name = "account")
public class Account extends BaseEntity {

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "userId", nullable = false)
	private User user;

	@Column(name = "accountId", nullable = false)
	private String accountId;

	@Column(name = "providerId", nullable = false)
	private String providerId;

	@Column(name = "accessToken")
	private String accessToken;

	@Column(name = "refreshToken")
	private String refreshToken;

	@Column(name = "accessTokenExpiresAt")
	private Instant accessTokenExpiresAt;

	@Column(name = "refreshTokenExpiresAt")
	private Instant refreshTokenExpiresAt;

	@Column(name = "scope")
	private String scope;

	@Column(name = "idToken")
	private String idToken;

	@Column(name = "password")
	private String password;

}
