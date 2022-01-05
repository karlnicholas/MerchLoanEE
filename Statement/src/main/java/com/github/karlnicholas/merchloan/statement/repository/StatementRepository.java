package com.github.karlnicholas.merchloan.statement.repository;

import com.github.karlnicholas.merchloan.statement.model.Statement;
import com.github.karlnicholas.merchloan.statement.model.StatementPK;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StatementRepository extends JpaRepository<Statement, StatementPK> {
}
