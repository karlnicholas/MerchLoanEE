package com.github.karlnicholas.merchloan.accounts.repository;

import com.github.karlnicholas.merchloan.accounts.model.RegisterEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface RegisterEntryRepository extends JpaRepository<RegisterEntry, UUID> {
    List<RegisterEntry> findByLoanIdOrderByRowNum(UUID loanId);

    List<RegisterEntry> findByLoanIdAndDateBetweenOrderByRowNum(UUID loanId, LocalDate startDate, LocalDate endDate);

    List<RegisterEntry> findByLoanIdAndDateAfterOrderByRowNum(UUID loanId, LocalDate statementDate);
}
