package com.github.karlnicholas.merchloan.accounts.repository;

import com.github.karlnicholas.merchloan.accounts.model.Loan;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LoanRepository {
    List<Loan> findByLoanState(Loan.LOAN_STATE open);
}
