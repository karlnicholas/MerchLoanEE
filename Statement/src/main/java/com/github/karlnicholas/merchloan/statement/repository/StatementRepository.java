package com.github.karlnicholas.merchloan.statement.repository;

import com.github.karlnicholas.merchloan.statement.model.Statement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface StatementRepository extends JpaRepository<Statement, UUID> {
    Optional<Statement> findFirstByLoanIdOrderByStatementDateDesc(UUID loanId);

    Optional<Statement> findByLoanIdAndStatementDate(UUID loanId, LocalDate statementDate);
}
