package com.github.karlnicholas.merchloan.accounts.repository;

import com.github.karlnicholas.merchloan.accounts.model.LoanState;

import java.util.Optional;
import java.util.UUID;

public interface LoanStateRepository {

//    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<LoanState> findById(UUID id);

}