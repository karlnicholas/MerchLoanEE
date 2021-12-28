package com.github.karlnicholas.merchloan.accounts.service;

import com.github.karlnicholas.merchloan.accounts.model.Account;
import com.github.karlnicholas.merchloan.accounts.model.Lender;
import com.github.karlnicholas.merchloan.accounts.model.Loan;
import com.github.karlnicholas.merchloan.accounts.repository.AccountRepository;
import com.github.karlnicholas.merchloan.accounts.repository.LenderRepository;
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

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class AccountManagementService {
    private final AccountRepository accountRepository;
    private final LenderRepository lenderRepository;
    private final LoanRepository loanRepository;
    private final RabbitMqSender rabbitMqSender;

    @Autowired
    public AccountManagementService(AccountRepository accountRepository, LenderRepository lenderRepository, LoanRepository loanRepository, RabbitMqSender rabbitMqSender) {
        this.accountRepository = accountRepository;
        this.lenderRepository = lenderRepository;
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
            serviceRequest.setStatus("Success");
            serviceRequest.setStatusMessage("Success");

        } catch (DuplicateKeyException dke) {
            log.warn("Create Account duplicate key exception: {}", dke.getMessage());
            serviceRequest.setStatus("Failure");
            serviceRequest.setStatusMessage(dke.getMessage());
        }
        rabbitMqSender.sendServiceRequest(serviceRequest);
    }

    public void fundAccount(FundLoan fundLoan) {
        Lender lender = lenderRepository.findByLender(fundLoan.getLender()).orElseGet(() ->
                persistLender(fundLoan)
        );
        Optional<Account> accountQ = accountRepository.findById(fundLoan.getAccountId());
        if (accountQ.isPresent()) {
            Loan loan = loanRepository.save(
                    Loan.builder()
                            .id(fundLoan.getId())
                            .account(accountQ.get())
                            .lender(lender)
                            .startDate(fundLoan.getStartDate())
                            .build());
            rabbitMqSender.sendDebitAccount(
                    DebitAccount.builder()
                            .id(fundLoan.getId())
                            .amount(fundLoan.getAmount())
                            .date(fundLoan.getStartDate())
                            .loanId(loan.getId())
                            .build()
            );
        } else {
            rabbitMqSender.sendServiceRequest(
                    ServiceRequestResponse.builder()
                            .id(fundLoan.getId())
                            .status("Failure")
                            .statusMessage("Account not found for " + fundLoan.getAccountId())
                            .build()
            );
        }
    }

    private Lender persistLender(FundLoan fundLoan) {
        return lenderRepository.findByLender(fundLoan.getLender()).orElseGet(() -> {
            Lender lender = Lender.builder()
                    .id(UUID.randomUUID())
                    .lender(fundLoan.getLender())
                    .createDate(LocalDate.now()).build();
            boolean retry;
            do {
                retry = false;
                try {
                    lenderRepository.save(lender);
                } catch (DuplicateKeyException dke) {
                    lender.setId(UUID.randomUUID());
                    retry = true;
                }
            } while (retry);
            return lender;
        });
    }
}
