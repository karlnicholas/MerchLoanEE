package com.github.karlnicholas.merchloan.statement.service;

import com.github.karlnicholas.merchloan.statement.model.Statement;
import com.github.karlnicholas.merchloan.statement.repository.StatementRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class QueryService {
    private final StatementRepository statementRepository;

    public QueryService(StatementRepository statementRepository) {
        this.statementRepository = statementRepository;
    }

    public Optional<Statement> findById(UUID id) {
        return statementRepository.findById(id);
    }

}
