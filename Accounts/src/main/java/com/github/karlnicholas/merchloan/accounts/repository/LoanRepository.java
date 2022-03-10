package com.github.karlnicholas.merchloan.accounts.repository;

import com.github.karlnicholas.merchloan.accounts.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LoanRepository extends JpaRepository<Loan, UUID> {
    List<Loan> findByLoanState(Loan.LOAN_STATE open);
}
