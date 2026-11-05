package com.kora.backend.repository;

import com.kora.backend.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session, String> {

	Optional<Session> findByToken(String token);

	List<Session> findAllByUser_Id(String userId);

	long deleteByUser_Id(String userId);

	long deleteByExpiresAtBefore(Instant cutoff);

}
