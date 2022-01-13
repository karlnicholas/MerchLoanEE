package com.github.karlnicholas.merchloan.accounts.service;

import com.github.karlnicholas.merchloan.accounts.model.Account;
import com.github.karlnicholas.merchloan.accounts.model.Loan;
import com.github.karlnicholas.merchloan.accounts.repository.AccountRepository;
import com.github.karlnicholas.merchloan.accounts.repository.LoanRepository;
import com.github.karlnicholas.merchloan.jmsmessage.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class AccountManagementService {
    private final AccountRepository accountRepository;
    private final LoanRepository loanRepository;

    @Autowired
    public AccountManagementService(AccountRepository accountRepository, LoanRepository loanRepository) {
        this.accountRepository = accountRepository;
        this.loanRepository = loanRepository;
    }

    public ServiceRequestResponse createAccount(CreateAccount createAccount) {
        ServiceRequestResponse serviceRequest = ServiceRequestResponse.builder().id(createAccount.getId()).build();
        try {
            accountRepository.save(Account.builder()
                    .id(createAccount.getId())
                    .customer(createAccount.getCustomer())
                    .createDate(createAccount.getCreateDate())
                    .build()
            );
            serviceRequest.setStatus(ServiceRequestResponse.STATUS.SUCCESS);
            serviceRequest.setStatusMessage("Account created");

        } catch (DuplicateKeyException dke) {
            log.warn("Create Account duplicate key exception: {}", dke.getMessage());
            if (createAccount.getRetryCount() == 0) {
                serviceRequest.setStatus(ServiceRequestResponse.STATUS.FAILURE);
            } else {
                serviceRequest.setStatus(ServiceRequestResponse.STATUS.SUCCESS);
            }
            serviceRequest.setStatusMessage(dke.getMessage());
        }
        return serviceRequest;
    }

    public ServiceRequestResponse fundAccount(FundLoan fundLoan) {
        ServiceRequestResponse serviceRequest = ServiceRequestResponse.builder()
                .id(fundLoan.getId())
                .build();
        Optional<Account> accountQ = accountRepository.findById(fundLoan.getAccountId());
        if (accountQ.isPresent()) {
            try {
                loanRepository.save(
                        Loan.builder()
                                .id(fundLoan.getId())
                                .account(accountQ.get())
                                .startDate(fundLoan.getStartDate())
                                .build());
                serviceRequest.setStatus(ServiceRequestResponse.STATUS.SUCCESS);
                serviceRequest.setStatusMessage("Loan created");
            } catch (DuplicateKeyException dke) {
                log.warn("Create Account duplicate key exception: {}", dke.getMessage());
                if (fundLoan.getRetryCount() == 0) {
                    serviceRequest.setStatus(ServiceRequestResponse.STATUS.FAILURE);
                    serviceRequest.setStatusMessage(dke.getMessage());
                }
            }
        } else {
            serviceRequest.setStatus(ServiceRequestResponse.STATUS.FAILURE);
            serviceRequest.setStatusMessage("Account not found for " + fundLoan.getAccountId());
        }
        return serviceRequest;
    }

    public ServiceRequestResponse validateLoan(UUID loanId) {
        ServiceRequestResponse serviceRequest = ServiceRequestResponse.builder()
                .id(loanId)
                .build();
        Optional<Loan> loanQ = loanRepository.findById(loanId);
        if (loanQ.isPresent()) {
            serviceRequest.setStatus(ServiceRequestResponse.STATUS.SUCCESS);
            serviceRequest.setStatusMessage("Loan Validated");
        } else {
            serviceRequest.setStatus(ServiceRequestResponse.STATUS.FAILURE);
            serviceRequest.setStatusMessage("Loan not found for " + loanId);
        }
        return serviceRequest;
    }

    public ServiceRequestResponse statementHeader(StatementHeader statementHeader) {
        ServiceRequestResponse serviceRequest = ServiceRequestResponse.builder()
                .id(statementHeader.getId())
                .build();
        Optional<Account> accountQ = accountRepository.findById(statementHeader.getAccountId());
        if (accountQ.isPresent()) {
            statementHeader.setCustomer(accountQ.get().getCustomer());
            serviceRequest.setStatus(ServiceRequestResponse.STATUS.SUCCESS);
            serviceRequest.setStatusMessage("Statement header");
        } else {
            serviceRequest.setStatus(ServiceRequestResponse.STATUS.FAILURE);
            serviceRequest.setStatusMessage("Account not found for " + statementHeader.getAccountId());
        }
        return serviceRequest;

    }
}
