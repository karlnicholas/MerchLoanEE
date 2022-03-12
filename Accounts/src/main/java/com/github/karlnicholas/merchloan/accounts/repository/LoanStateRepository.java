package com.github.karlnicholas.merchloan.accounts.repository;

import com.github.karlnicholas.merchloan.accounts.model.LoanState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import javax.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;

public interface LoanStateRepository extends JpaRepository<LoanState, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<LoanState> findById(UUID id);

}