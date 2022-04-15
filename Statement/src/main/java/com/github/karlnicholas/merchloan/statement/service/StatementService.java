package com.github.karlnicholas.merchloan.statement.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.karlnicholas.merchloan.dto.RegisterEntryDto;
import com.github.karlnicholas.merchloan.dto.StatementDto;
import com.github.karlnicholas.merchloan.jmsmessage.StatementHeader;
import com.github.karlnicholas.merchloan.statement.dao.StatementDao;
import com.github.karlnicholas.merchloan.statement.model.Statement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class StatementService {
    private final StatementDao statementDao;
    private final ObjectMapper objectMapper;
    private final DataSource dataSource;

    public StatementService(StatementDao statementDao, DataSource dataSource) {
        this.statementDao = statementDao;
        this.dataSource = dataSource;
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
            StatementDto statementDto = StatementDto.builder()
                    .id(statementHeader.getId())
                    .loanId(statementHeader.getLoanId())
                    .accountId(statementHeader.getAccountId())
                    .customer(statementHeader.getCustomer())
                    .statementDate(statementHeader.getStatementDate())
                    .endDate(statementHeader.getEndDate())
                    .startDate(statementHeader.getStartDate())
                    .registerEntries(
                            statementHeader.getRegisterEntries()
                                    .stream()
                                    .map(
                                            re -> RegisterEntryDto.builder()
                                                    .rowNum(re.getRowNum())
                                                    .date(re.getDate())
                                                    .description(re.getDescription())
                                                    .credit(re.getCredit())
                                                    .debit(re.getDebit())
                                                    .balance(re.getBalance())
                                                    .build()
                                    ).collect(Collectors.toList())
                    ).build();
            Statement statement = Statement.builder()
                    .id(statementHeader.getId())
                    .loanId(statementHeader.getLoanId())
                    .statementDate(statementHeader.getStatementDate())
                    .statement(objectMapper.writeValueAsString(statementDto))
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
