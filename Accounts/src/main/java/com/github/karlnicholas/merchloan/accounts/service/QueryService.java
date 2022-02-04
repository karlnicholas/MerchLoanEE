package com.github.karlnicholas.merchloan.accounts.service;

import com.github.karlnicholas.merchloan.accounts.model.Account;
import com.github.karlnicholas.merchloan.accounts.model.Loan;
import com.github.karlnicholas.merchloan.accounts.repository.AccountRepository;
import com.github.karlnicholas.merchloan.accounts.repository.LoanRepository;
import com.github.karlnicholas.merchloan.dto.LoanDto;
import com.github.karlnicholas.merchloan.jms.message.RabbitMqSender;
import com.github.karlnicholas.merchloan.jmsmessage.MostRecentStatement;
import com.github.karlnicholas.merchloan.jmsmessage.RegisterEntry;
import com.github.karlnicholas.merchloan.jmsmessage.StatementHeader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class QueryService {
    private final AccountRepository accountRepository;
    private final LoanRepository loanRepository;
    private final RabbitMqSender rabbitMqSender;

    @Autowired
    public QueryService(AccountRepository accountRepository, LoanRepository loanRepository, RabbitMqSender rabbitMqSender) {
        this.accountRepository = accountRepository;
        this.loanRepository = loanRepository;
        this.rabbitMqSender = rabbitMqSender;
    }

    public Optional<Account> queryAccountId(UUID id) {
        return accountRepository.findById(id);
    }
    public Optional<LoanDto> queryLoanId(UUID loanId) {
        // get last statement
        // get register entries
        // return last statement date
        // return last statement ending balance
        // return current balance
        // return payoff amount
        // loan has AccountId, startDate, funding, months, interestRate, monthlyPayment, loanState
        Optional<Loan> loanOpt = loanRepository.findById(loanId);
        if ( loanOpt.isPresent() ) {
            Loan loan = loanOpt.get();
            MostRecentStatement mostRecentStatement = (MostRecentStatement) rabbitMqSender.queryMostRecentStatement(loanId);
            StatementHeader statementHeader = StatementHeader.builder().build();
            statementHeader.setLoanId(loanId);
            if ( mostRecentStatement.getStatementDate() == null ) {
                statementHeader.setEndDate(loan.getStartDate().plusMonths(1));
                statementHeader.setStartDate(loan.getStartDate());
            } else {
                statementHeader.setEndDate(statementHeader.getStatementDate().plusMonths(1));
                statementHeader.setStartDate(statementHeader.getStatementDate().plusDays(1));
            }
            statementHeader = (StatementHeader) rabbitMqSender.registerStatementHeader(statementHeader);
            LoanDto loanDto = LoanDto.builder()
                    .loanId(loanId)
                    .accountId(loan.getAccount().getId())
                    .customer(loan.getAccount().getCustomer())
                    .funding(loan.getFunding())
                    .loanState(loan.getLoanState().name())
                    .interestRate(loan.getInterestRate())
                    .startDate(loan.getStartDate())
                    .monthlyPayments(loan.getMonthlyPayments())
                    .months(loan.getMonths())
                    .build();
            // return last statement date
            // return last statement ending balance
            // return current balance
            // return payoff amount
            BigDecimal startingBalance;
            BigDecimal interestBalance;
            if ( statementHeader.getStatementDate() == null ) {
                startingBalance = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN);
                interestBalance = loan.getFunding();
            } else {
                startingBalance = mostRecentStatement.getEndingBalance();
                interestBalance = mostRecentStatement.getEndingBalance();
            }
            BigDecimal payoffAmount = interestBalance.multiply(loan.getInterestRate()).divide(BigDecimal.valueOf(loan.getMonths()), RoundingMode.HALF_EVEN);
            BigDecimal currentBalance = startingBalance;
            for (RegisterEntry re: statementHeader.getRegisterEntries()) {
                if ( re.getDebit() != null ) {
                    currentBalance = currentBalance.add(re.getDebit());
                } else if (re.getCredit() != null) {
                    currentBalance = currentBalance.subtract(re.getCredit());
                }
            }
            payoffAmount = payoffAmount.add(currentBalance);
            loanDto.setPayoffAmount(payoffAmount);
            loanDto.setCurrentBalance(currentBalance);
            if ( statementHeader.getStatementDate() != null ) {
                loanDto.setLastStatementDate(mostRecentStatement.getStatementDate());
                loanDto.setLastStatementBalance(mostRecentStatement.getEndingBalance());
            }
            return Optional.of(loanDto);
        } else {
            return Optional.empty();
        }
    }

}
