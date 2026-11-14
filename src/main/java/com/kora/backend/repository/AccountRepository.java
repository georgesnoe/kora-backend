package com.kora.backend.repository;

import com.kora.backend.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, String> {

	Optional<Account> findByProviderIdAndAccountId(String providerId, String accountId);

	List<Account> findAllByUserId(String userId);

	Optional<Account> findByUserIdAndProviderId(String userId, String providerId);

}
