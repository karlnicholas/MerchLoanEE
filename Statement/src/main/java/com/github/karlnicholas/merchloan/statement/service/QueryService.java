package com.github.karlnicholas.merchloan.statement.service;

import com.github.karlnicholas.merchloan.statement.repository.StatementRepository;
import org.springframework.stereotype.Service;

@Service
public class QueryService {
    private final StatementRepository statementRepository;

    public QueryService(StatementRepository statementRepository) {
        this.statementRepository = statementRepository;
    }
}
