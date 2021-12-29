package com.github.karlnicholas.merchloan.accounts.service;

import com.github.karlnicholas.merchloan.accounts.model.Account;
import com.github.karlnicholas.merchloan.accounts.model.Loan;
import com.github.karlnicholas.merchloan.accounts.repository.AccountRepository;
import com.github.karlnicholas.merchloan.accounts.repository.LoanRepository;
import com.github.karlnicholas.merchloan.jms.message.RabbitMqSender;
import com.github.karlnicholas.merchloan.jmsmessage.CreateAccount;
import com.github.karlnicholas.merchloan.jmsmessage.DebitAccount;
import com.github.karlnicholas.merchloan.jmsmessage.FundLoan;
import com.github.karlnicholas.merchloan.jmsmessage.ServiceRequestResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class AccountManagementService {
    private final AccountRepository accountRepository;
    private final LoanRepository loanRepository;
    private final RabbitMqSender rabbitMqSender;

    @Autowired
    public AccountManagementService(AccountRepository accountRepository, LoanRepository loanRepository, RabbitMqSender rabbitMqSender) {
        this.accountRepository = accountRepository;
        this.loanRepository = loanRepository;
        this.rabbitMqSender = rabbitMqSender;
    }

    public void createAccount(CreateAccount createAccount) {
        ServiceRequestResponse serviceRequest = ServiceRequestResponse.builder().id(createAccount.getId()).build();
        try {
            accountRepository.save(Account.builder()
                    .id(createAccount.getId())
                    .customer(createAccount.getCustomer())
                    .createDate(createAccount.getCreateDate())
                    .build()
            );
            serviceRequest.setStatus(ServiceRequestResponse.STATUS.SUCCESS);
            serviceRequest.setStatusMessage("Success");

        } catch (DuplicateKeyException dke) {
            log.warn("Create Account duplicate key exception: {}", dke.getMessage());
            if (createAccount.getRetryCount() == 0) {
                serviceRequest.setStatus(ServiceRequestResponse.STATUS.FAILURE);
            } else {
                serviceRequest.setStatus(ServiceRequestResponse.STATUS.SUCCESS);
            }
            serviceRequest.setStatusMessage(dke.getMessage());
        }
        rabbitMqSender.sendServiceRequest(serviceRequest);
    }

    public void fundAccount(FundLoan fundLoan) {
        Optional<Account> accountQ = accountRepository.findById(fundLoan.getAccountId());
        if (accountQ.isPresent()) {
            try {
                Loan loan = loanRepository.save(
                        Loan.builder()
                                .id(fundLoan.getId())
                                .account(accountQ.get())
                                .startDate(fundLoan.getStartDate())
                                .build());

                rabbitMqSender.sendDebitAccount(
                        DebitAccount.builder()
                                .id(fundLoan.getId())
                                .amount(fundLoan.getAmount())
                                .date(fundLoan.getStartDate())
                                .loanId(loan.getId())
                                .description(fundLoan.getDescription())
                                .build()
                );
            } catch (DuplicateKeyException dke) {
                log.warn("Create Account duplicate key exception: {}", dke.getMessage());
                if (fundLoan.getRetryCount() == 0) {
                    rabbitMqSender.sendServiceRequest(ServiceRequestResponse.builder()
                            .id(fundLoan.getId())
                            .status(ServiceRequestResponse.STATUS.FAILURE)
                            .statusMessage(dke.getMessage())
                            .build());
                }
            }
        } else {
            rabbitMqSender.sendServiceRequest(
                    ServiceRequestResponse.builder()
                            .id(fundLoan.getId())
                            .status(ServiceRequestResponse.STATUS.FAILURE)
                            .statusMessage("Account not found for " + fundLoan.getAccountId())
                            .build()
            );
        }
    }
}
