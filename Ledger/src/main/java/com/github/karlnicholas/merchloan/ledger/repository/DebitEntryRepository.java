package com.github.karlnicholas.merchloan.ledger.repository;

import com.github.karlnicholas.merchloan.ledger.model.DebitEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DebitEntryRepository extends JpaRepository<DebitEntry, UUID> {
}
