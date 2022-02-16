package com.github.karlnicholas.merchloan.statement.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.karlnicholas.merchloan.jmsmessage.StatementHeader;
import com.github.karlnicholas.merchloan.statement.model.Statement;
import com.github.karlnicholas.merchloan.statement.repository.StatementRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class StatementService {
    private final StatementRepository statementRepository;
    private final ObjectMapper objectMapper;

    public StatementService(StatementRepository statementRepository) {
        this.statementRepository = statementRepository;
        this.objectMapper = new ObjectMapper().findAndRegisterModules()
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    }

    public Optional<Statement> findStatement(UUID loanId, LocalDate statementDate) {
        return statementRepository.findByLoanIdAndStatementDate(
                loanId,
                statementDate
        );
    }

    public Statement saveStatement(StatementHeader statementHeader, BigDecimal startingBalance, BigDecimal endingBalance) throws JsonProcessingException {
        return statementRepository.save(Statement.builder()
                .id(statementHeader.getId())
                .loanId(statementHeader.getLoanId())
                .statementDate(statementHeader.getStatementDate())
                .statement(objectMapper.writeValueAsString(statementHeader))
                .startingBalance(startingBalance)
                .endingBalance(endingBalance)
                .build()
        );
    }

    public Optional<Statement> findLastStatement(UUID loanId) {
        return statementRepository.findFirstByLoanIdOrderByStatementDateDesc(loanId);
    }

}
