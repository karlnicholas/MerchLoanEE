package com.github.karlnicholas.merchloan.ledger.repository;

import com.github.karlnicholas.merchloan.ledger.model.CreditEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CreditEntryRepository extends JpaRepository<CreditEntry, UUID> {
}
