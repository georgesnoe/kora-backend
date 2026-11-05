package com.kora.backend.repository;

import com.kora.backend.entity.Verification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationRepository extends JpaRepository<Verification, String> {

	Optional<Verification> findByIdentifier(String identifier);

	Optional<Verification> findByIdentifierAndValue(String identifier, String value);

	long deleteByIdentifier(String identifier);

}
