package com.github.karlnicholas.merchloan.statement.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.karlnicholas.merchloan.dto.RegisterEntryDto;
import com.github.karlnicholas.merchloan.dto.StatementDto;
import com.github.karlnicholas.merchloan.jmsmessage.RegisterEntryMessage;
import com.github.karlnicholas.merchloan.jmsmessage.StatementHeader;
import com.github.karlnicholas.merchloan.statement.dao.StatementDao;
import com.github.karlnicholas.merchloan.statement.model.Statement;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
@Slf4j
public class StatementService {
    @Inject
    private StatementDao statementDao;
    private final ObjectMapper objectMapper;
    @Resource(lookup = "java:jboss/datasources/StatementDS")
    private DataSource dataSource;

    public StatementService() {
        this.objectMapper = new ObjectMapper().findAndRegisterModules()
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    }

    public Optional<Statement> findStatement(UUID loanId, LocalDate statementDate) throws SQLException {
        try (Connection con = dataSource.getConnection()) {
            return statementDao.findByLoanIdAndStatementDate(con,
                    loanId,
                    statementDate
            );
        }
    }

    public Statement saveStatement(StatementHeader statementHeader, BigDecimal startingBalance, BigDecimal endingBalance) throws JsonProcessingException, SQLException {
        try (Connection con = dataSource.getConnection()) {
            List<RegisterEntryDto> registerEntries = new ArrayList<>();
            for (int i = 1; i <= statementHeader.getRegisterEntries().size(); i++) {
                RegisterEntryMessage rem = statementHeader.getRegisterEntries().get(i - 1);
                registerEntries.add(RegisterEntryDto.builder()
                        .rowNum(i)
                        .date(rem.getDate())
                        .description(rem.getDescription())
                        .credit(rem.getCredit())
                        .debit(rem.getDebit())
                        .balance(rem.getBalance())
                        .build());
            }
            StatementDto statementDto = StatementDto.builder()
                    .id(statementHeader.getId())
                    .loanId(statementHeader.getLoanId())
                    .accountId(statementHeader.getAccountId())
                    .customer(statementHeader.getCustomer())
                    .statementDate(statementHeader.getStatementDate())
                    .endDate(statementHeader.getEndDate())
                    .startDate(statementHeader.getStartDate())
                    .registerEntries(registerEntries)
                    .build();
            Statement statement = Statement.builder()
                    .id(statementHeader.getId())
                    .loanId(statementHeader.getLoanId())
                    .statementDate(statementHeader.getStatementDate())
                    .statementDoc(objectMapper.writeValueAsString(statementDto))
                    .startingBalance(startingBalance)
                    .endingBalance(endingBalance)
                    .build();
            statementDao.insert(con, statement);
            return statement;
        }
    }

    public Optional<Statement> findLastStatement(UUID loanId) throws SQLException {
        try (Connection con = dataSource.getConnection()) {
            return statementDao.findFirstByLoanIdOrderByStatementDateDesc(con, loanId);
        }
    }

}
