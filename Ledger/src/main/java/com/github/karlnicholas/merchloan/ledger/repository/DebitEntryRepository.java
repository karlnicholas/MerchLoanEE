package com.github.karlnicholas.merchloan.ledger.repository;

import com.github.karlnicholas.merchloan.ledger.model.DebitEntry;
import com.github.karlnicholas.merchloan.ledger.model.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface DebitEntryRepository extends JpaRepository<DebitEntry, UUID> {
    List<DebitEntry> findAllByLoanId(UUID id);
}
