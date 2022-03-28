package com.github.karlnicholas.merchloan.accounts.service;

import com.github.karlnicholas.merchloan.accounts.model.Account;
import com.github.karlnicholas.merchloan.accounts.model.Loan;
import com.github.karlnicholas.merchloan.accounts.model.RegisterEntry;
import com.github.karlnicholas.merchloan.accounts.repository.AccountRepository;
import com.github.karlnicholas.merchloan.accounts.repository.LoanRepository;
import com.github.karlnicholas.merchloan.accounts.repository.RegisterEntryRepository;
import com.github.karlnicholas.merchloan.dto.LoanDto;
import com.github.karlnicholas.merchloan.jms.message.RabbitMqSender;
import com.github.karlnicholas.merchloan.jmsmessage.MostRecentStatement;
import com.github.karlnicholas.merchloan.jmsmessage.StatementHeader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class QueryService {
    private final AccountRepository accountRepository;
    private final LoanRepository loanRepository;
    private final RabbitMqSender rabbitMqSender;
    private final RegisterEntryRepository registerEntryRepository;

    @Autowired
    public QueryService(AccountRepository accountRepository, LoanRepository loanRepository, RabbitMqSender rabbitMqSender, RegisterEntryRepository registerEntryRepository) {
        this.accountRepository = accountRepository;
        this.loanRepository = loanRepository;
        this.rabbitMqSender = rabbitMqSender;
        this.registerEntryRepository = registerEntryRepository;
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
            // start building response
            LoanDto loanDto = LoanDto.builder()
                    .loanId(loanId)
                    .accountId(loan.getAccount().getId())
                    .customer(loan.getAccount().getCustomer())
                    .funding(loan.getFunding())
                    .loanState(loan.getLoanState().name())
                    .interestRate(loan.getInterestRate())
                    .startDate(loan.getStartDate())
                    .statementDates(loan.getStatementDates())
                    .monthlyPayments(loan.getMonthlyPayments())
                    .months(loan.getMonths())
                    .build();
            if ( loan.getLoanState() != Loan.LOAN_STATE.CLOSED ) {
                computeLoanValues(loanId, loan, loanDto);
            }
            return Optional.of(loanDto);
        } else {
            return Optional.empty();
        }
    }

    private void computeLoanValues(UUID loanId, Loan loan, LoanDto loanDto) {
        // get most recent statement
        MostRecentStatement mostRecentStatement = (MostRecentStatement) rabbitMqSender.queryMostRecentStatement(loanId);
        // generate a simulated new statement for current period
        StatementHeader statementHeader = StatementHeader.builder().build();
        statementHeader.setLoanId(loanId);
        List<RegisterEntry> registerEntries = new ArrayList<>();
        if (mostRecentStatement.getStatementDate() == null) {
            statementHeader.setEndDate(loan.getStatementDates().get(0));
            statementHeader.setStartDate(loan.getStartDate());
            registerEntries.addAll(registerEntryRepository.findByLoanIdAndDateBetweenOrderByRowNum(statementHeader.getLoanId(), statementHeader.getStartDate(), statementHeader.getEndDate()));
        } else {
            int index = loan.getStatementDates().indexOf(mostRecentStatement.getStatementDate());
            if ( index+1 < loan.getStatementDates().size() ) {
                statementHeader.setEndDate(loan.getStatementDates().get(index+1));
                statementHeader.setStartDate(loan.getStatementDates().get(index).plusDays(1));
                registerEntries.addAll(registerEntryRepository.findByLoanIdAndDateBetweenOrderByRowNum(statementHeader.getLoanId(), statementHeader.getStartDate(), statementHeader.getEndDate()));
            }
        }
//        List<RegisterEntry> registerEntries = registerEntryRepository.findByLoanIdAndDateBetweenOrderByRowNum(statementHeader.getLoanId(), statementHeader.getStartDate(), statementHeader.getEndDate());
        // determine current balance, payoff amount
        BigDecimal startingBalance;
        BigDecimal interestBalance;
        if (mostRecentStatement.getStatementDate() == null) {
            startingBalance = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_EVEN);
            interestBalance = loan.getFunding();
        } else {
            startingBalance = mostRecentStatement.getEndingBalance();
            interestBalance = mostRecentStatement.getEndingBalance();
        }
        BigDecimal currentBalance = startingBalance;
        // determine current payoff amount
        BigDecimal currentInterest = interestBalance.multiply(loan.getInterestRate()).divide(BigDecimal.valueOf(loan.getMonths()), RoundingMode.HALF_EVEN);
        for (RegisterEntry re : registerEntries) {
            if (re.getDebit() != null) {
                currentBalance = currentBalance.add(re.getDebit());
            } else if (re.getCredit() != null) {
                currentBalance = currentBalance.subtract(re.getCredit());
            }
        }
        BigDecimal payoffAmount = currentInterest.add(currentBalance).setScale(2, RoundingMode.HALF_EVEN);
        // compute current Payment
        // must first compute expected balance
        // what is number of months?
        int nMonths = loan.getStatementDates().indexOf(statementHeader.getEndDate()) + 1;
        BigDecimal computeAmount = loan.getFunding();
        for (int i = 0; i < nMonths; ++i) {
            BigDecimal computeInterest = computeAmount.multiply(loan.getInterestRate()).divide(BigDecimal.valueOf(loan.getMonths()), RoundingMode.HALF_EVEN);
            computeAmount = computeAmount.add(computeInterest).subtract(loan.getMonthlyPayments()).setScale(2, RoundingMode.HALF_EVEN);
        }
        // fill out additional response
        loanDto.setCurrentPayment(currentBalance.add(currentInterest).setScale(2, RoundingMode.HALF_EVEN).subtract(computeAmount));
        loanDto.setCurrentInterest(currentInterest.setScale(2, RoundingMode.HALF_EVEN));
        loanDto.setPayoffAmount(payoffAmount);
        loanDto.setCurrentBalance(currentBalance);
        if (mostRecentStatement.getStatementDate() != null) {
            loanDto.setLastStatementDate(mostRecentStatement.getStatementDate());
            loanDto.setLastStatementBalance(mostRecentStatement.getEndingBalance());
        }
    }

    public List<RegisterEntry> queryRegisterByLoanId(UUID id) {
        return registerEntryRepository.findByLoanIdOrderByRowNum(id);
    }
}
