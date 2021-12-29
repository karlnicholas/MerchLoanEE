package com.github.karlnicholas.merchloan.accounts.service;

import com.github.karlnicholas.merchloan.accounts.model.Account;
import com.github.karlnicholas.merchloan.accounts.model.Loan;
import com.github.karlnicholas.merchloan.accounts.repository.AccountRepository;
import com.github.karlnicholas.merchloan.accounts.repository.LoanRepository;
import com.github.karlnicholas.merchloan.jms.message.RabbitMqSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class QueryService {
    private final AccountRepository accountRepository;
    private final LoanRepository loanRepository;

    @Autowired
    public QueryService(AccountRepository accountRepository, LoanRepository loanRepository, RabbitMqSender rabbitMqSender) {
        this.accountRepository = accountRepository;
        this.loanRepository = loanRepository;
    }

    public Optional<Account> queryAccountId(UUID id) {
        return accountRepository.findById(id);
    }
    public Optional<Loan> queryLoanId(UUID id) {
        return loanRepository.findById(id);
    }
}
