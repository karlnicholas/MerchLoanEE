package com.github.karlnicholas.merchloan.register.service;

import com.github.karlnicholas.merchloan.jmsmessage.CreditLoan;
import com.github.karlnicholas.merchloan.jmsmessage.DebitLoan;
import com.github.karlnicholas.merchloan.jmsmessage.ServiceRequestResponse;
import com.github.karlnicholas.merchloan.jmsmessage.StatementHeader;
import com.github.karlnicholas.merchloan.register.model.LoanState;
import com.github.karlnicholas.merchloan.register.model.RegisterEntry;
import com.github.karlnicholas.merchloan.register.repository.LoanStateRepository;
import com.github.karlnicholas.merchloan.register.repository.RegisterEntryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public ServiceRequestResponse fundLoan(DebitLoan fundLoan) {
        ServiceRequestResponse requestResponse = ServiceRequestResponse.builder()
                .id(fundLoan.getId())
                .build();
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
            if ( fundLoan.getRetryCount() == 0 ) {
                requestResponse.setFailure(dke.getMessage());
            } else {
                requestResponse.setSuccess("Funding transaction entered");
            }
        }
        return requestResponse;
    }

    public ServiceRequestResponse debitLoan(DebitLoan debitLoan) {
        ServiceRequestResponse requestResponse = ServiceRequestResponse.builder()
                .id(debitLoan.getId())
                .build();
        try {
            RegisterEntry debitEntry = RegisterEntry.builder()
                    .id(debitLoan.getId())
                    .loanId(debitLoan.getLoanId())
                    .date(debitLoan.getDate())
                    .debit(debitLoan.getAmount())
                    .description(debitLoan.getDescription())
                    .build();
            Optional<LoanState> loanStateOpt = loanStateRepository.findById(debitLoan.getLoanId());
            if ( loanStateOpt.isPresent() ) {
                LoanState loanState = loanStateOpt.get();
                loanState.setCurrentRowNum(loanState.getCurrentRowNum()+1);
                loanState.setBalance(loanState.getBalance().add(debitEntry.getDebit()));
                debitEntry.setRowNum(loanState.getCurrentRowNum());
                registerEntryRepository.save(debitEntry);
                loanStateRepository.save(loanState);
            }
            requestResponse.setSuccess("Debit transaction entered");
        } catch (DuplicateKeyException dke) {
            log.warn("ServiceRequestResponse debitLoan(DebitLoan debitLoan) duplicate key: {}", dke.getMessage());
            if ( debitLoan.getRetryCount() == 0 ) {
                requestResponse.setFailure(dke.getMessage());
            } else {
                requestResponse.setSuccess("Debit transaction entered");
            }
        }
        return requestResponse;
    }

    public ServiceRequestResponse creditLoan(CreditLoan creditLoan) {
        ServiceRequestResponse requestResponse = ServiceRequestResponse.builder()
                .id(creditLoan.getId())
                .build();
        try {
            RegisterEntry creditEntry = RegisterEntry.builder()
                    .id(creditLoan.getId())
                    .loanId(creditLoan.getLoanId())
                    .date(creditLoan.getDate())
                    .credit(creditLoan.getAmount())
                    .description(creditLoan.getDescription())
                    .build();
            Optional<LoanState> loanStateOpt = loanStateRepository.findById(creditLoan.getLoanId());
            if ( loanStateOpt.isPresent() ) {
                LoanState loanState = loanStateOpt.get();
                loanState.setCurrentRowNum(loanState.getCurrentRowNum()+1);
                loanState.setBalance(loanState.getBalance().subtract(creditEntry.getCredit()));
                creditEntry.setRowNum(loanState.getCurrentRowNum());
                registerEntryRepository.save(creditEntry);
                loanStateRepository.save(loanState);
            }
            requestResponse.setSuccess("Credit transaction entered");
        } catch (DuplicateKeyException dke) {
            log.warn("ServiceRequestResponse creditLoan(CreditLoan creditLoan) duplicate key: {}", dke.getMessage());
            if ( creditLoan.getRetryCount() == 0 ) {
                requestResponse.setFailure(dke.getMessage());
            } else {
                requestResponse.setSuccess("Credit transaction entered");
            }
        }
        return requestResponse;
    }

    public ServiceRequestResponse statementHeader(StatementHeader statementHeader) {
        ServiceRequestResponse requestResponse = ServiceRequestResponse.builder()
                .id(statementHeader.getId())
                .build();
        try {
            List<RegisterEntry> entries = registerEntryRepository.findByLoanIdAndDateBetweenOrderByRowNum(statementHeader.getLoanId(), statementHeader.getStartDate(), statementHeader.getEndDate());
            statementHeader.setRegisterEntries(new ArrayList<>());
            entries.forEach(e->statementHeader.getRegisterEntries().add(
                    com.github.karlnicholas.merchloan.jmsmessage.RegisterEntry.builder()
                            .rowNum(e.getRowNum())
                            .date(e.getDate())
                            .credit(e.getCredit())
                            .debit(e.getDebit())
                            .description(e.getDescription())
                            .build()));
            requestResponse.setSuccess("Statement header");
        } catch (DuplicateKeyException dke) {
            log.warn("ServiceRequestResponse creditLoan(CreditLoan creditLoan) duplicate key: {}", dke.getMessage());
            if ( statementHeader.getRetryCount() == 0 ) {
                requestResponse.setFailure(dke.getMessage());
            } else {
                requestResponse.setSuccess("Statement header");
            }
        }
        return requestResponse;
    }

//    private void ThreadSleep(long time) {
//        try {
//            Thread.sleep(time);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
}
