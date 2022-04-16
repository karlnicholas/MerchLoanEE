package com.github.karlnicholas.merchloan.accounts.service;

import com.github.karlnicholas.merchloan.accounts.dao.LoanStateDao;
import com.github.karlnicholas.merchloan.accounts.dao.RegisterEntryDao;
import com.github.karlnicholas.merchloan.accounts.model.LoanState;
import com.github.karlnicholas.merchloan.accounts.model.RegisterEntry;
import com.github.karlnicholas.merchloan.jmsmessage.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@Transactional
public class RegisterManagementService {
    private final DataSource dataSource;
    private final LoanStateDao loanStateDao;
    private final RegisterEntryDao registerEntryDao;

    public RegisterManagementService(DataSource dataSource, LoanStateDao loanStateDao, RegisterEntryDao registerEntryDao) {
        this.dataSource = dataSource;
        this.loanStateDao = loanStateDao;
        this.registerEntryDao = registerEntryDao;
    }

    public void fundLoan(DebitLoan fundLoan, ServiceRequestResponse requestResponse) throws SQLException {
        try (Connection con = dataSource.getConnection()) {
            loanStateDao.insert(con,
                    LoanState.builder()
                            .loanId(fundLoan.getLoanId())
                            .startDate(fundLoan.getDate())
                            .balance(fundLoan.getAmount())
                            .currentRowNum(1)
                            .build());
            registerEntryDao.insert(con, RegisterEntry.builder()
                    .id(fundLoan.getId())
                    .loanId(fundLoan.getLoanId())
                    .date(fundLoan.getDate())
                    .debit(fundLoan.getAmount())
                    .description(fundLoan.getDescription())
                    .rowNum(1)
                    .build());
            requestResponse.setSuccess("Funding transaction entered");
        } catch (DuplicateKeyException dke) {
            log.warn("ServiceRequestResponse createLoan(CreateLoan createLoan) duplicate key: {}", dke.getMessage());
            if (fundLoan.getRetry().booleanValue()) {
                requestResponse.setSuccess("Funding transaction entered");
            } else {
                requestResponse.setFailure(dke.getMessage());
            }
        }
    }

    public void debitLoan(DebitLoan debitLoan, ServiceRequestResponse requestResponse) throws SQLException {
        try (Connection con = dataSource.getConnection()) {
            RegisterEntry debitEntry = RegisterEntry.builder()
                    .id(debitLoan.getId())
                    .loanId(debitLoan.getLoanId())
                    .date(debitLoan.getDate())
                    .debit(debitLoan.getAmount())
                    .description(debitLoan.getDescription())
                    .build();
//            loanStateDao.readWriteTablesLock(con);
            Optional<LoanState> loanStateOpt = loanStateDao.findByLoanIdForUpdate(con, debitLoan.getLoanId());
            if (loanStateOpt.isPresent()) {
                LoanState loanState = loanStateOpt.get();
                loanState.setCurrentRowNum(loanState.getCurrentRowNum() + 1);
                loanState.setBalance(loanState.getBalance().add(debitEntry.getDebit()));
                debitEntry.setRowNum(loanState.getCurrentRowNum());
                registerEntryDao.insert(con, debitEntry);
                loanStateDao.updateWithCommit(con, loanState);
            }
//            loanStateDao.tablesUnlock(con);
            requestResponse.setSuccess("Debit transaction entered");
        } catch (DuplicateKeyException dke) {
            log.warn("ServiceRequestResponse debitLoan(DebitLoan debitLoan) duplicate key: {}", dke.getMessage());
            if (debitLoan.getRetry().booleanValue()) {
                requestResponse.setSuccess("Debit transaction entered");
            } else {
                requestResponse.setFailure(dke.getMessage());
            }
        }
    }

    public void creditLoan(CreditLoan creditLoan, ServiceRequestResponse requestResponse) throws SQLException {
        try (Connection con = dataSource.getConnection()) {
            RegisterEntry creditEntry = RegisterEntry.builder()
                    .id(creditLoan.getId())
                    .loanId(creditLoan.getLoanId())
                    .date(creditLoan.getDate())
                    .credit(creditLoan.getAmount())
                    .description(creditLoan.getDescription())
                    .build();
            Optional<LoanState> loanStateOpt = loanStateDao.findByLoanIdForUpdate(con, creditLoan.getLoanId());
            if (loanStateOpt.isPresent()) {
                LoanState loanState = loanStateOpt.get();
                loanState.setCurrentRowNum(loanState.getCurrentRowNum() + 1);
                BigDecimal newBalance = loanState.getBalance().subtract(creditEntry.getCredit());
//                if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
//                    requestResponse.setFailure("Credit results in negative balance: " + newBalance);
//                } else {
                loanState.setBalance(newBalance);
                creditEntry.setRowNum(loanState.getCurrentRowNum());
                registerEntryDao.insert(con, creditEntry);
                loanStateDao.updateWithCommit(con, loanState);
                requestResponse.setSuccess("Credit transaction entered");
//                }
            } else {
                requestResponse.setFailure("LoanId not found: " + creditLoan.getLoanId());
            }
        } catch (DuplicateKeyException dke) {
            log.warn("ServiceRequestResponse creditLoan(CreditLoan creditLoan) duplicate key: {}", dke.getMessage());
            if (creditLoan.getRetry().booleanValue()) {
                requestResponse.setSuccess("Credit transaction entered");
            } else {
                requestResponse.setFailure(dke.getMessage());
            }
        }
    }

    public void setStatementHeaderRegisterEntryies(StatementHeader statementHeader) throws SQLException {
        try (Connection con = dataSource.getConnection()) {
            List<RegisterEntry> entries = registerEntryDao.findByLoanIdAndDateBetweenOrderByRowNum(con, statementHeader.getLoanId(), statementHeader.getStartDate(), statementHeader.getEndDate());
            entries.forEach(e -> statementHeader.getRegisterEntries().add(
                    RegisterEntryMessage.builder()
                            .rowNum(e.getRowNum())
                            .date(e.getDate())
                            .credit(e.getCredit())
                            .debit(e.getDebit())
                            .description(e.getDescription())
                            .build()));
        }
    }

    public RegisterEntry billingCycleCharge(BillingCycleCharge billingCycleCharge) throws SQLException {
        try (Connection con = dataSource.getConnection()) {
            if (billingCycleCharge.getRetry().booleanValue()) {
                Optional<RegisterEntry> reOpt = registerEntryDao.findById(con, billingCycleCharge.getId());
                if (reOpt.isPresent()) {
                    return reOpt.get();
                }
            }
            RegisterEntry registerEntry = RegisterEntry.builder()
                    .id(billingCycleCharge.getId())
                    .loanId(billingCycleCharge.getLoanId())
                    .date(billingCycleCharge.getDate())
                    .debit(billingCycleCharge.getDebit())
                    .credit(billingCycleCharge.getCredit())
                    .description(billingCycleCharge.getDescription())
                    .build();
            Optional<LoanState> loanStateOpt = loanStateDao.findByLoanIdForUpdate(con, billingCycleCharge.getLoanId());
            if (loanStateOpt.isPresent()) {
                LoanState loanState = loanStateOpt.get();
                loanState.setCurrentRowNum(loanState.getCurrentRowNum() + 1);
                if (registerEntry.getDebit() != null) {
                    loanState.setBalance(loanState.getBalance().add(registerEntry.getDebit()));
                } else {
                    loanState.setBalance(loanState.getBalance().subtract(registerEntry.getCredit()));
                }
                registerEntry.setRowNum(loanState.getCurrentRowNum());
                billingCycleCharge.setRowNum(loanState.getCurrentRowNum());
                registerEntryDao.insert(con, registerEntry);
                loanStateDao.updateWithCommit(con, loanState);
            }
            return registerEntry;
        } catch (DuplicateKeyException dke) {
            log.warn("ServiceRequestResponse debitLoan(DebitLoan debitLoan) duplicate key: {}", dke.getMessage());
            throw dke;
        }
    }
}
