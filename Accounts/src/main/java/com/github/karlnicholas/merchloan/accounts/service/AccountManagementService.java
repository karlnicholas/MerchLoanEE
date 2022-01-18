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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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
        ServiceRequestResponse requestResponse = ServiceRequestResponse.builder().id(createAccount.getId()).build();
        try {
            accountRepository.save(Account.builder()
                    .id(createAccount.getId())
                    .customer(createAccount.getCustomer())
                    .createDate(createAccount.getCreateDate())
                    .build()
            );
            requestResponse.setSuccess("Account created");

        } catch (DuplicateKeyException dke) {
            log.warn("Create Account duplicate key exception: {}", dke.getMessage());
            if (createAccount.getRetryCount() == 0) {
                requestResponse.setFailure(dke.getMessage());
            } else {
                requestResponse.setSuccess("Account created");
            }
        }
        return requestResponse;
    }

    public ServiceRequestResponse fundAccount(FundLoan fundLoan) {
        ServiceRequestResponse requestResponse = ServiceRequestResponse.builder()
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
                requestResponse.setSuccess();
            } catch (DuplicateKeyException dke) {
                log.warn("Create Account duplicate key exception: {}", dke.getMessage());
                if (fundLoan.getRetryCount() == 0) {
                    requestResponse.setFailure(dke.getMessage());
                }
            }
        } else {
            requestResponse.setFailure("Account not found for " + fundLoan.getAccountId());
        }
        return requestResponse;
    }

    public ServiceRequestResponse validateLoan(UUID loanId) {
        ServiceRequestResponse requestResponse = ServiceRequestResponse.builder()
                .id(loanId)
                .build();
        Optional<Loan> loanQ = loanRepository.findById(loanId);
        if (loanQ.isPresent()) {
            requestResponse.setSuccess();
        } else {
            requestResponse.setFailure("Loan not found for " + loanId);
        }
        return requestResponse;
    }

    public ServiceRequestResponse statementHeader(StatementHeader statementHeader) {
        ServiceRequestResponse requestResponse = ServiceRequestResponse.builder()
                .id(statementHeader.getId())
                .build();
        Optional<Account> accountQ = accountRepository.findById(statementHeader.getAccountId());
        if (accountQ.isPresent()) {
            statementHeader.setCustomer(accountQ.get().getCustomer());
            requestResponse.setSuccess();
        } else {
            requestResponse.setFailure("Account not found for " + statementHeader.getAccountId());
        }
        return requestResponse;

    }

    //TODO: close loans
    public List<UUID> loansToCycle(LocalDate businessDate) {
        return loanRepository.findAll().stream()
                .filter(l ->
                        !businessDate.isEqual(l.getStartDate())
                                && (
                                (businessDate.lengthOfMonth() == businessDate.getDayOfMonth() && l.getStartDate().lengthOfMonth() > businessDate.lengthOfMonth())
                                        || l.getStartDate().getDayOfMonth() == businessDate.getDayOfMonth()
                        )
                )
                .map(Loan::getId)
                .collect(Collectors.toList());
    }
}
