package com.github.karlnicholas.merchloan.ledger.repository;

import com.github.karlnicholas.merchloan.ledger.model.CreditEntry;
import com.github.karlnicholas.merchloan.ledger.model.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CreditEntryRepository extends JpaRepository<CreditEntry, UUID> {
    List<CreditEntry> findAllByLoanId(UUID loanId);
}
