package com.github.karlnicholas.merchloan.statement.repository;

import com.github.karlnicholas.merchloan.statement.model.Statement;
import com.github.karlnicholas.merchloan.statement.model.StatementPK;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StatementRepository extends JpaRepository<Statement, StatementPK> {
    Optional<Statement> findFirstByIdLoanIdOrderByIdStatementDateDesc(UUID loanId);

}
