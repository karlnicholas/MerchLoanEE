package com.github.karlnicholas.merchloan.accounts.service;

import com.github.karlnicholas.merchloan.accounts.model.Account;
import com.github.karlnicholas.merchloan.accounts.model.Lender;
import com.github.karlnicholas.merchloan.accounts.model.Loan;
import com.github.karlnicholas.merchloan.accounts.repository.AccountRepository;
import com.github.karlnicholas.merchloan.accounts.repository.LenderRepository;
import com.github.karlnicholas.merchloan.accounts.repository.LoanRepository;
import com.github.karlnicholas.merchloan.jms.message.RabbitMqSender;
import com.github.karlnicholas.merchloan.jmsmessage.CreateAccount;
import com.github.karlnicholas.merchloan.jmsmessage.DebitFromLoan;
import com.github.karlnicholas.merchloan.jmsmessage.FundLoan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

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
        try {
            accountRepository.save(Account.builder()
                    .id(createAccount.getId())
                    .customer(createAccount.getCustomer())
                    .createDate(createAccount.getCreateDate())
                    .build()
            );
        } catch (DuplicateKeyException dke) {
            log.warn("Create Account duplicate key exception: {}", dke.getMessage());
        }
    }

    public void fundAccount(FundLoan fundLoan) {
        Lender lender = lenderRepository.findByLender(fundLoan.getLender()).orElseGet(() ->
                persistLender(fundLoan)
        );
        accountRepository.findById(fundLoan.getAccountId()).ifPresent(account -> {
            Loan loan = loanRepository.save(
                    Loan.builder()
                            .id(fundLoan.getId())
                            .account(account)
                            .lender(lender)
                            .startDate(fundLoan.getStartDate())
                            .build());
            rabbitMqSender.sendDebitFr0mLoan(
                    DebitFromLoan.builder()
                            .id(fundLoan.getId())
                            .amount(fundLoan.getAmount())
                            .date(fundLoan.getStartDate())
                            .loanId(loan.getId())
                            .build()
            );
        });
    }

    private Lender persistLender(FundLoan fundLoan) {
        Lender lender = Lender.builder()
                .id(fundLoan.getId())
                .lender(fundLoan.getLender())
                .createDate(LocalDate.now()).build();
        try {
            return lenderRepository.save(lender);
        } catch (DuplicateKeyException dke) {
            log.error("Lender persistLender(FundLoan fundLoan) duplicate key: {}", dke.getMessage());
            return lender;
        }
    }
}
