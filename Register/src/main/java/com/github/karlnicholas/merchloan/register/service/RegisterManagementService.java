package com.github.karlnicholas.merchloan.register.service;

import com.github.karlnicholas.merchloan.apimessage.message.ServiceRequestMessage;
import com.github.karlnicholas.merchloan.jmsmessage.*;
import com.github.karlnicholas.merchloan.register.model.LoanState;
import com.github.karlnicholas.merchloan.register.model.RegisterEntry;
import com.github.karlnicholas.merchloan.register.repository.LoanStateRepository;
import com.github.karlnicholas.merchloan.register.repository.RegisterEntryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@Transactional
public class RegisterManagementService {
    private final RegisterEntryRepository registerEntryRepository;
    private final LoanStateRepository loanStateRepository;

    public RegisterManagementService(RegisterEntryRepository registerEntryRepository, LoanStateRepository loanStateRepository) {
        this.registerEntryRepository = registerEntryRepository;
        this.loanStateRepository = loanStateRepository;
    }

    public void fundLoan(DebitLoan fundLoan, ServiceRequestResponse requestResponse) {
        //TODO: Better logic here
        try {
            loanStateRepository.save(
                    LoanState.builder()
                            .loanId(fundLoan.getLoanId())
                            .startDate(fundLoan.getDate())
                            .balance(fundLoan.getAmount())
                            .currentRowNum(1)
                            .build());
            registerEntryRepository.save(RegisterEntry.builder()
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
            if (fundLoan.getRetry()) {
                requestResponse.setSuccess("Funding transaction entered");
            } else {
                requestResponse.setFailure(dke.getMessage());
            }
        }
    }

    public void debitLoan(DebitLoan debitLoan, ServiceRequestResponse requestResponse) {
        try {
            RegisterEntry debitEntry = RegisterEntry.builder()
                    .id(debitLoan.getId())
                    .loanId(debitLoan.getLoanId())
                    .date(debitLoan.getDate())
                    .debit(debitLoan.getAmount())
                    .description(debitLoan.getDescription())
                    .build();
            Optional<LoanState> loanStateOpt = loanStateRepository.findById(debitLoan.getLoanId());
            if (loanStateOpt.isPresent()) {
                LoanState loanState = loanStateOpt.get();
                loanState.setCurrentRowNum(loanState.getCurrentRowNum() + 1);
                loanState.setBalance(loanState.getBalance().add(debitEntry.getDebit()));
                debitEntry.setRowNum(loanState.getCurrentRowNum());
                registerEntryRepository.save(debitEntry);
                loanStateRepository.save(loanState);
            }
            requestResponse.setSuccess("Debit transaction entered");
        } catch (DuplicateKeyException dke) {
            log.warn("ServiceRequestResponse debitLoan(DebitLoan debitLoan) duplicate key: {}", dke.getMessage());
            if (debitLoan.getRetry()) {
                requestResponse.setSuccess("Debit transaction entered");
            } else {
                requestResponse.setFailure(dke.getMessage());
            }
        }
    }

    public void creditLoan(CreditLoan creditLoan, ServiceRequestResponse requestResponse) {
        try {
            RegisterEntry creditEntry = RegisterEntry.builder()
                    .id(creditLoan.getId())
                    .loanId(creditLoan.getLoanId())
                    .date(creditLoan.getDate())
                    .credit(creditLoan.getAmount())
                    .description(creditLoan.getDescription())
                    .build();
            Optional<LoanState> loanStateOpt = loanStateRepository.findById(creditLoan.getLoanId());
            if (loanStateOpt.isPresent()) {
                LoanState loanState = loanStateOpt.get();
                loanState.setCurrentRowNum(loanState.getCurrentRowNum() + 1);
                BigDecimal newBalance = loanState.getBalance().subtract(creditEntry.getCredit());
                if (newBalance.compareTo(BigDecimal.ZERO) <= 0) {
                    requestResponse.setFailure("Credit results in zero or less balance: " + newBalance + " Make close loan request." );
                } else {
                    loanState.setBalance(newBalance);
                    creditEntry.setRowNum(loanState.getCurrentRowNum());
                    registerEntryRepository.save(creditEntry);
                    loanStateRepository.save(loanState);
                    requestResponse.setSuccess("Credit transaction entered");
                }
            } else {
                requestResponse.setFailure("LoanId not found: " + creditLoan.getLoanId());
            }
        } catch (DuplicateKeyException dke) {
            log.warn("ServiceRequestResponse creditLoan(CreditLoan creditLoan) duplicate key: {}", dke.getMessage());
            if (creditLoan.getRetry()) {
                requestResponse.setSuccess("Credit transaction entered");
            } else {
                requestResponse.setFailure(dke.getMessage());
            }
        }
    }

    public void statementHeader(StatementHeader statementHeader) {
        try {
            List<RegisterEntry> entries = registerEntryRepository.findByLoanIdAndDateBetweenOrderByRowNum(statementHeader.getLoanId(), statementHeader.getStartDate(), statementHeader.getEndDate());
            statementHeader.setRegisterEntries(new ArrayList<>());
            entries.forEach(e -> statementHeader.getRegisterEntries().add(
                    com.github.karlnicholas.merchloan.jmsmessage.RegisterEntry.builder()
                            .rowNum(e.getRowNum())
                            .date(e.getDate())
                            .credit(e.getCredit())
                            .debit(e.getDebit())
                            .description(e.getDescription())
                            .build()));
        } catch (DuplicateKeyException dke) {
            log.warn("ServiceRequestResponse creditLoan(CreditLoan creditLoan) duplicate key: {}", dke.getMessage());
        }
    }

    public void billingCycleCharge(BillingCycleCharge billingCycleCharge) {
        try {
            RegisterEntry registerEntry = RegisterEntry.builder()
                    .id(billingCycleCharge.getId())
                    .loanId(billingCycleCharge.getLoanId())
                    .date(billingCycleCharge.getDate())
                    .debit(billingCycleCharge.getDebit())
                    .credit(billingCycleCharge.getCredit())
                    .description(billingCycleCharge.getDescription())
                    .build();
            Optional<LoanState> loanStateOpt = loanStateRepository.findById(billingCycleCharge.getLoanId());
            if (loanStateOpt.isPresent()) {
                LoanState loanState = loanStateOpt.get();
                loanState.setCurrentRowNum(loanState.getCurrentRowNum() + 1);
                if ( registerEntry.getDebit() != null ) {
                    loanState.setBalance(loanState.getBalance().add(registerEntry.getDebit()));
                } else {
                    loanState.setBalance(loanState.getBalance().subtract(registerEntry.getCredit()));
                }
                registerEntry.setRowNum(loanState.getCurrentRowNum());
                billingCycleCharge.setRowNum(loanState.getCurrentRowNum());
                registerEntryRepository.save(registerEntry);
                loanStateRepository.save(loanState);
            }
            billingCycleCharge.setSuccess(ServiceRequestMessage.STATUS.SUCCESS.name());
        } catch (DuplicateKeyException dke) {
            log.warn("ServiceRequestResponse debitLoan(DebitLoan debitLoan) duplicate key: {}", dke.getMessage());
            if ( !billingCycleCharge.getRetry()) {
                billingCycleCharge.setFailure(dke.getMessage());
            }
        }
    }

//    private void ThreadSleep(long time) {
//        try {
//            Thread.sleep(time);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
}
