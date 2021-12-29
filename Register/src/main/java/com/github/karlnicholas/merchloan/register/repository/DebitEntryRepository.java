package com.github.karlnicholas.merchloan.register.repository;

import com.github.karlnicholas.merchloan.register.model.DebitEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DebitEntryRepository extends JpaRepository<DebitEntry, UUID> {
    List<DebitEntry> findAllByLoanId(UUID id);
}
